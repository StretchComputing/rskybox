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
      try {
        response.audioUrl = '/audio/' + response.appId + '/' + response.id;
        return response;
      } catch (e) {
        r.log.error(e, 'Feedback.parse');
      }
    },
  });


  r.FeedbackList = r.BaseCollection.extend({
    model: r.Feedback,
    apiUrl: '/feedback',

    parse: function (response) {
      try {
        return response.feedback;
      } catch (e) {
        r.log.error(e, 'FeedbackList.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
