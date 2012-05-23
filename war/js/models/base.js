var RSKYBOX = (function (r, $) {
  'use strict';


  r.Base = {
    // The REST base portion of the URL, including the version.
    restUrl: '/rest/v1',
    appUrl: '/applications/',

    // Sets the model's URL using a base REST url and the API url.
    // If there is an ID, set the urlRoot for use outside of a collection.
    setUrl: function () {
      try {
        var url;

        if (!this.apiUrl) {
          r.log.warn('invalid apiUrl', 'r.Base.setUrl');
          this.url = '';
          return false;
        }

        url = this.restUrl + this.apiUrl;
        if (this.get('id')) {
          this.urlRoot = url;
          delete this.url;
        } else {
          this.url = url;
        }
        return true;
      } catch (e) {
        r.log.error(e, 'Model:Base.setUrl');
      }
    },

    setAppUrl: function (appId) {
      try {
        var url;

        if (!this.apiUrl || !appId) {
          r.log.warn('invalid apiUrl (' + this.apiUrl + ') or appId (' + appId + ')', 'r.Base.setAppUrl');
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
      } catch (e) {
        r.log.error(e, 'Model:Base.setAppUrl');
      }
    },
  };


  r.BaseModelExtended = Backbone.Model.extend(r.Base);


  r.BaseModel = r.BaseModelExtended.extend({
    constructor: function () {
      Backbone.Model.prototype.constructor.apply(this, arguments);

      // Partial updates are enabled by overriding toJSON. (see toJSON below)
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
          try {
            return Object.keys(fields).length > 0;
          } catch (e) {
            r.log.error(e, 'BaseModel.constructor.partial.any');
          }
        };

        // model: the model that's being saved
        // attrs: attributes to be partially updated
        // options: backbone save options, including ajax handlers
        // force: whether or not to proceed with update even if no changes were made
        partial.save = function (model, attrs, options, force) {
          try {
            var proceed = false;

            // Set the fields that have changed.
            Object.keys(attrs).forEach(function (key) {
              if (force || model.get(key) !== attrs[key]) {
                partial.setField(key);
                proceed = true;
              }
            });

            if (proceed) {
              options.headers = options.headers || {};
              options.headers.background = true;
              model.save(attrs, options);
            }
            partial.clear();
          } catch (e) {
            r.log.error(e, 'BaseModel.constructor.partial.save');
          }
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
      try {
        if (this.isNew()) {
          Object.keys(this.attributes).forEach(function (key) {
            if (!this.get(key)) {
              this.unset(key, {silent: true});
            }
          }, this);
        }
        return this;
      } catch (e) {
        r.log.error(e, 'BaseModel.prepareNewModel');
      }
    },

    // Gets a mock object for use in HTML forms. Set up a 'fields' attribute in the model
    // that has all the form/model fields in order to use this method.
    getMock: function () {
      try {
        var field, mock = {};

        if (!this.fields) {
          r.log.warn('No fields defined for model', 'BaseModel.getMock');
          return;
        }

        Object.keys(this.fields).forEach(function (field) {
          mock[field] = this.get(field) || '';
        }, this);
        return mock;
      } catch (e) {
        r.log.error(e, 'BaseModel.getMock');
      }
    },

    isFieldBeingUpdated: function (field) {
      try {
        return field && (!this.partial.any() || this.partial.getFields()[field]);
      } catch (e) {
        r.log.error(e, 'BaseModel.isFieldBeingUpdated');
      }
    },

    // Partial updates work because we intercept toJSON when we want to work with
    // a subset of fields from the model.
    toJSON: function () {
      try {
        var json = {};

        if (this.partial.any()) {
          Object.keys(this.partial.getFields()).forEach(function (field) {
            json[field] = this.get(field);
          }, this);
          return json;
        }

        // This is the exact line from backbone's toJSON method.
        return _.clone(this.attributes);
      } catch (e) {
        r.log.error(e, 'BaseModel.toJSON');
      }
    },
  });


  r.BaseCollection = Backbone.Collection.extend(r.Base);


  return r;
}(RSKYBOX || {}, jQuery));
