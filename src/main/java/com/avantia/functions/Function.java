package com.avantia.functions;

import java.util.*;
import com.microsoft.azure.serverless.functions.annotation.*;
import com.microsoft.azure.serverless.functions.*;

//import org.json.*;
//import org.json.JSONObject;

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
			@HttpTrigger(name = "request", methods = {"get", "post"}, 
			authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage request,
			final ExecutionContext context) throws Exception {
		
		context.getLogger().info("Java HTTP trigger processed a request.");

		Object body = request.getBody();

		@SuppressWarnings("unchecked")          // 2017-11-19 JMC eliminate compiler warning
		HashMap<String, String> requestJSON = (HashMap<String, String>) body;   // application/json converts to Java LinkedHashMap

		context.getLogger().info("#####################");
		context.getLogger().info("Input Values: " + requestJSON);

		// Create H2O object
		hex.genmodel.GenModel rawModel;
		rawModel = (hex.genmodel.GenModel) Class.forName(modelClassName).newInstance();
		EasyPredictModelWrapper model = new EasyPredictModelWrapper(rawModel);
	    
		//Getting String values  inside hash map:
		String AGE = requestJSON.get("AGE");
		String RACE = requestJSON.get("RACE");
		String PSA = requestJSON.get("PSA");
		String GLEASON = requestJSON.get("GLEASON");

		context.getLogger().info(AGE);
				
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

		//context.getLogger().info(p.label);
		modelPrediction = p.label;

		return request.createResponse(200, "Label (aka prediction) is: " + modelPrediction);
		//}

	}
}
