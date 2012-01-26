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
    },

    // Unsets all attributes that are undefined, null, '', or 0 in prepartion
    // for a new model to be saved.
    //
    // !!! This function should be used with caution !!!
    //
    prepareNewModel: function() {
      _.each(_.keys(this.attributes), function(key) {
        if (!this.get(key)) {
          this.unset(key, {silent: true});
        }
      }, this);
      return this;
    }
  };


  r.BaseModel = Backbone.Model.extend(r.Base);


  r.BaseCollection = Backbone.Collection.extend(r.Base);


  return r;
})(rskybox || {}, jQuery);
