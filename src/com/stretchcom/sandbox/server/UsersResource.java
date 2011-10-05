package com.stretchcom.sandbox.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class UsersResource extends ServerResource {
    private static final Logger log = Logger.getLogger(UsersResource.class.getName());
    private String id;

    @Override
    protected void doInit() throws ResourceException {
        log.info("in doInit");
        id = (String) getRequest().getAttributes().get("id");
    }

    @Get("json")
    public JsonRepresentation get(Variant variant) {
        log.info("in get");
        if (id != null) {
            return show(id);
        } else {
            return index();
        }
    }

    @Post("json")
    public JsonRepresentation post(Representation entity) {
        log.info("in post");
        return new JsonRepresentation(save_user(entity));
    }

    @Put("json")
    public JsonRepresentation put(Representation entity) {
        log.info("in put");
        return new JsonRepresentation(save_user(entity));
    }

    @Delete("json")
    public JsonRepresentation delete() {
        log.info("in delete");
        EntityManager em = EMF.get().createEntityManager();
        try {
            Key key = KeyFactory.stringToKey(id);
            em.getTransaction().begin();
            User user = (User) em.createNamedQuery("User.getByKey").setParameter("key", key).getSingleResult();
            em.remove(user);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        return null;
    }
    private JsonRepresentation index() {
        log.info("in index");
        JSONObject json = new JSONObject();
        EntityManager em = EMF.get().createEntityManager();
        try {
            List<User> users = new ArrayList<User>();
            JSONArray ja = new JSONArray();
            users = (List<User>) em.createNamedQuery("User.getAll").getResultList();
            for (User user : users) {
                ja.put(get_json(user));
            }
            json.put("users", ja);
        } catch (JSONException e) {
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new JsonRepresentation(json);
    }

    private JsonRepresentation show(String id) {
        log.info("in show");
        EntityManager em = EMF.get().createEntityManager();
        Key key = KeyFactory.stringToKey(id);
        User user = (User) em.createNamedQuery("User.getByKey").setParameter("key", key).getSingleResult();

        return new JsonRepresentation(get_json(user));
    }

    private JSONObject save_user(Representation entity) {
        EntityManager em = EMF.get().createEntityManager();

        User user = null;
        em.getTransaction().begin();
        try {
            user = new User();
            JSONObject json = new JsonRepresentation(entity).getJsonObject();
            if (id != null) {
                Key key = KeyFactory.stringToKey(id);
                user = (User) em.createNamedQuery("User.getByKey").setParameter("key", key).getSingleResult();
            }
            if (json.has("first_name")) {
                user.setFirstName(json.getString("first_name"));
            }
            if (json.has("last_name")) {
                user.setLastName(json.getString("last_name"));
            }
            if (json.has("email_address")) {
                user.setEmailAddress(json.getString("email_address"));
            }
            em.persist(user);
            em.getTransaction().commit();
        } catch (IOException e) {
            log.severe("error extracting JSON object from Post");
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        return get_json(user);
    }

    private JSONObject get_json(User user) {
        JSONObject json = new JSONObject();

        try {
//            json.put("user_id", KeyFactory.keyToString(user.getKey()));
            json.put("first_name", user.getFirstName());
            json.put("last_name", user.getLastName());
            json.put("email_address", user.getEmailAddress());
        } catch (JSONException e) {
            e.printStackTrace();
            this.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return json;
    }
}
