var RSKYBOX = (function (r, $) {
  'use strict';


  r.Base = {
    // The REST base portion of the URL, including the version.
    restUrl: '/rest/v1',
    appUrl: '/applications/',

    // Sets the model's URL using a base REST url and the API url.
    // If there is an ID, set the urlRoot for use outside of a collection.
    setUrl: function () {
      var url;

      if (!this.apiUrl) {
        r.log.error('invalid apiUrl');
        this.url = '';
        return false;
      }

      url = this.restUrl + this.apiUrl;
      if (this.get('id')) {
        this.urlRoot = url;
      } else {
        this.url = url;
      }
      return true;
    },

    setAppUrl: function (appId) {
      var url;

      if (!this.apiUrl || !appId) {
        r.log.error('invalid apiUrl (' + this.apiUrl + ') or appId (' + appId + ')');
        this.url = '';
        return false;
      }

      url = this.restUrl + this.appUrl + appId + this.apiUrl;
      if (this.get('id')) {
        this.urlRoot = url;
      } else {
        this.url = url;
      }
      return true;
    },
  };


  r.BaseModelExtended = Backbone.Model.extend(r.Base);


  r.BaseModel = r.BaseModelExtended.extend({
    constructor: function () {
      Backbone.Model.prototype.constructor.apply(this, arguments);

      this.partial = (function () {
        var fields = {}, partial = {};

        partial.setField = function (field) {
          fields[field] = true;
        };

        partial.getFields = function () {
          return fields;
        };

        partial.clear = function () {
          this.fields = {};
        };

        partial.any = function () {
          return Object.keys(fields).length > 0;
        };

        // model: the model that's being saved
        // attrs: attributes to be partially updated
        // handlers: object containing the ajax handlers
        partial.save = function (model, attrs, handlers) {
          var proceed = false;

          // Set the fields that have changed.
          Object.keys(attrs).forEach(function (key) {
            if (model.get(key) !== attrs[key]) {
              partial.setField(key);
              proceed = true;
            }
          });

          if (proceed) {
            model.save(attrs, handlers);
          }
          partial.clear();
        };

        return partial;
      }());

    },

    // Unsets all attributes that are undefined, null, '', or 0 in prepartion
    // for a new model to be saved.
    //
    // !!! This function should be used with caution !!!
    //
    prepareNewModel: function () {
      if (this.isNew()) {
        Object.keys(this.attributes).forEach(function (key) {
          if (!this.get(key)) {
            this.unset(key, {silent: true});
          }
        }, this);
      }
      return this;
    },

    // Gets a mock object for use in HTML forms. Set up a 'fields' attribute in the model
    // that has all the form/model fields in order to use this method.
    getMock: function () {
      var field, mock = {};

      if (!this.fields) {
        r.log.error('No fields defined for model:');
        return;
      }

      Object.keys(this.fields).forEach(function (field) {
        mock[field] = this.get(field) || null;
      }, this);
      return mock;
    },

    isFieldBeingUpdated: function (field) {
      return field && (!this.partial.any() || this.partial.getFields()[field]);
    },

    toJSON: function () {
      var json = {};

      if (this.partial.any()) {
        Object.keys(this.partial.getFields()).forEach(function (field) {
          json[field] = this.get(field);
        }, this);
        return json;
      }

      // This is the exact line from backbone's toJSON method.
      return _.clone(this.attributes);
    },
  });


  r.BaseCollection = Backbone.Collection.extend(r.Base);


  return r;
}(RSKYBOX || {}, jQuery));
