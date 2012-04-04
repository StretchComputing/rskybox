package com.stretchcom.rskybox.server;

import java.io.IOException;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class DatastoreResource extends ServerResource {
	private static final Logger log = Logger.getLogger(DatastoreResource.class.getName());

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
    }

    // Handles 'Migration' API
    @Put 
    public JsonRepresentation handleMigration(Representation entity) {
    	JSONObject jsonReturn = new JSONObject();
    	log.info("handleMigration(@Put) entered ..... private migration");
		
		String apiStatus = ApiStatusCode.SUCCESS;
		this.setStatus(Status.SUCCESS_OK);
		
        try {
			JsonRepresentation jsonRep = new JsonRepresentation(entity);
			JSONObject json = jsonRep.getJsonObject();
			log.info("received json object = " + json.toString());
			
			if(json.has("secret")) {
				String secret = json.getString("secret");
				if(secret.equals("ae53b1f9")) {
					if(json.has("migrationName")) {
						String migrationName = json.getString("migrationName");
						
						// Migration that are tasks are identified by a migration name that ends with "Task"
						if(migrationName.endsWith("Task")) {
							createMigrationTask(migrationName);
						} else {
							log.severe("migration must be a task");
						}
					}
					else {
						log.severe("migration called with no 'migrationName' parameter");
					}
				}
			}
        } catch (IOException e) {
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
			log.severe(":handleMigration:IOException error extracting JSON object from Post");
		} catch (JSONException e) {
			this.setStatus(Status.SERVER_ERROR_INTERNAL);
			log.severe(":handleMigration:JSONException1");
		} finally {
		}
		
		try {
			jsonReturn.put("apiStatus", apiStatus);
		} catch (JSONException e) {
			log.severe(":handleMigration:JSONException2");
		}
		return new JsonRepresentation(jsonReturn);
    }

    private void createMigrationTask(String theMigrationName) {
		// URL "/migrationTask" is routed to MigrationTaskServlet in web.xml
		// not calling name() to name the task, so the GAE will assign a unique name that has not been used in 7 days (see book)
		// method defaults to POST, but decided to be explicit here
		// PRIORITY TODO need to somehow secure this URL. Book uses web.xml <security-constraint> but not sure why it restricts the
		//               URL to task queues (I see how it restricts it to admins)
		TaskOptions taskOptions = TaskOptions.Builder.withUrl("/migrationTask")
				.method(Method.POST)
				.param("migrationName", theMigrationName);
		Queue queue = QueueFactory.getQueue("migration"); // "migration" queue is defined in WEB-INF/queue.xml
		queue.add(taskOptions);
    }
}
