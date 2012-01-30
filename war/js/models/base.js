'use strict';


var rskybox = (function(r, $) {


  r.Base = {
    // The REST base portion of the URL, including the version.
    restUrl: '/rest/v1',

    // Set apiUrl when creating the concrete model then call makeUrl.
    setUrl: function(apiUrl) {
      if (apiUrl) {
        this.url = this.restUrl + this.apiUrl;
      } else {
        r.log.error('invalid apiUrl: ' + apiUrl);
        this.url = '';
      }
    }
  };


  r.BaseModelExtended = Backbone.Model.extend(r.Base);

  r.BaseModel = r.BaseModelExtended.extend({
    // Unsets all attributes that are undefined, null, '', or 0 in prepartion
    // for a new model to be saved.
    //
    // !!! This function should be used with caution !!!
    //
    prepareNewModel: function() {
      if (this.isNew()) {
        _.each(_.keys(this.attributes), function(key) {
          if (!this.get(key)) {
            this.unset(key, {silent: true});
          }
        }, this);
      }
      return this;
    },

    // Gets a mock object for use in HTML forms. Set up a 'fields' attribute in the model
    // that has all the form/model fields in order to use this method.
    getMock: function() {
      var field, mock = {};

      if (!this.fields) {
        r.log.error('No fields defined for model:');
        return;
      }

      for (field in this.fields) {
        mock[field] = this.get(field) || null;
      }
      return mock;
    },
  });


  r.BaseCollection = Backbone.Collection.extend(r.Base);


  return r;
})(rskybox || {}, jQuery);
