var RSKYBOX = (function (r, $) {
  'use strict';


  r.EndpointFilter = r.BaseModel.extend({
    apiUrl: '/endpointFilters',
    fields: {
      id: null,
      appId: null,
      active: null,
      localEndPoint: null,
      remoteEndPoint: null
    }
  });


  r.EndpointFilters = r.BaseCollection.extend({
    model: r.EndpointFilter,
    apiUrl: '/endpointFilters',

    parse: function (response) {
      try {
        return response.endpointFilters;
      } catch (e) {
        r.log.error(e, 'EndpointFilters.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
