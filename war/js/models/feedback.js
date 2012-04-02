var RSKYBOX = (function (r, $) {
  'use strict';


  r.Feedback = r.BaseModel.extend({
    apiUrl: '/feedback',
    fields: {
      id: null,
      appId: null,
      date: null,
      userName: null,
      instanceUrl: null,
      status: null,
      audioUrl: null,
    },

    parse: function (response) {
      response.audioUrl = '/audio/' + response.appId + '/' + response.id;
      response.date = new Date(response.date);
      return response;
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
