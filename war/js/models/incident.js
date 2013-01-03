var RSKYBOX = (function (r, $) {
  'use strict';


  r.Incident = r.BaseModel.extend({
    apiUrl: '/incidents',
    fields: {
      id: null,
      appId: null,
      number: null,
      status: null,
      severity: null,
      name: null,
      lastUpdatedDate: null,
      createdDate: null,
      tags: null,
      eventCount: null,
      events: null,
      summary: null,
      mode: null,
      message: null,
      appActions: null,
			githubUrl: null,
    },
  });


  r.Incidents = r.BaseCollection.extend({
    model: r.Incident,
    apiUrl: '/incidents',

    parse: function (response) {
      try {
        return response.incidents;
      } catch (e) {
        r.log.error(e, 'Incidents.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
