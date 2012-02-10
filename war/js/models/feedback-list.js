var RSKYBOX = (function (r, $) {
  'use strict';


  r.Feedback = r.BaseModel.extend({
    fields: {
      id: null,
      appId: null,
      date: null,
      userName: null,
      instanceUrl: null,
      status: null,
    },

    audioUrl: function() {
      return '/audio/' + this.get('appId') + '/' + this.id;
    },
  });


  r.FeedbackList = r.BaseCollection.extend({
    model: r.Feedback,
    apiUrl: '/feedback',

    parse: function (response) {
      return response.feedback;
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
