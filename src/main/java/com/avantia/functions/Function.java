package com.avantia.functions;

import java.util.*;
import com.microsoft.azure.serverless.functions.annotation.*;
import com.microsoft.azure.serverless.functions.*;

import org.json.*;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
//import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.BinomialModelPrediction;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

	// Name of the generated H2O model
	private static String modelClassName =  "com.avantia.functions.models.GLM_model_R_1511970560428_1";

	// Prediction Value
	private static String modelPrediction = "unknown";

	/**
	 * This function listens at endpoint "/api/hello". Two ways to invoke it using "curl" command in bash:
	 * 1. curl -d "HTTP Body" {your host}/api/hello
	 * 2. curl {your host}/api/hello?name=HTTP%20Query
	 */
	@FunctionName("hello")
	public HttpResponseMessage<String> hello(
			@HttpTrigger(name = "req", methods = {"get", "post"}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			final ExecutionContext context) throws Exception {
		context.getLogger().info("Java HTTP trigger processed a request.");

		// Parse query parameter
		String query = request.getQueryParameters().get("name");
		String name = request.getBody().orElse(query);

		context.getLogger().info("#####################");
		context.getLogger().info("Input Values:" + name);

		// Create H2O object (see gbm_pojo_test.java)
		hex.genmodel.GenModel rawModel;
		rawModel = (hex.genmodel.GenModel) Class.forName(modelClassName).newInstance();
		EasyPredictModelWrapper model = new EasyPredictModelWrapper(rawModel);

		if (name == null) {
			return request.createResponse(400, "Please pass a name on the query string or in the request body");
		} else {

			JSONObject obj = new JSONObject(name);

			//Getting String values  inside JSONObject obj :
			String AGE = obj.getString("AGE");
			String RACE = obj.getString("RACE");
			String PSA = obj.getString("PSA");
			String GLEASON = obj.getString("GLEASON");

			context.getLogger().info("Age: " + AGE);
			
			RowData row = new RowData();
			row.put("AGE", AGE);
			row.put("RACE", RACE);
			row.put("PSA", PSA);
			row.put("GLEASON", GLEASON);

			BinomialModelPrediction p = null;
			//try {
			p = model.predictBinomial(row);
			//} catch (PredictException e) {
			//	e.printStackTrace();
			//}

			modelPrediction = p.label;

			return request.createResponse(200, "Label (aka prediction) is: " + modelPrediction);
		}
	}
}
