'use strict';


var rskybox = (function(r, $) {


  r.controller = {
    applicationsBeforeShow: function() {
      r.log.debug('applicationsBeforeShow');
      delete(r.applications);
      r.applications = new r.Applications();
      r.applicationsView = new r.ApplicationsView({
        el: r.getContentDiv(),
        collection: r.applications
      });
    },

    applicationsShow: function() {
      r.log.debug('applicationsShow');
      r.applications.fetch();
    },

    settingsBeforeShow: function() {
      r.log.debug('settingsBeforeShow');
    },

    settingsShow: function() {
      r.log.debug('settingsShow');
    },

    newAppBeforeShow: function() {
      r.log.debug('newAppBeforeShow');
      r.newApp = new r.Application();
      r.newAppView = new r.ApplicationView({
        model: r.newApp
      });
    },

  };

  r.router = new $.mobile.Router([
    { '#applications':  { handler: 'applicationsBeforeShow', events: 'bs' } },
    { '#applications':  { handler: 'applicationsShow', events: 's' } },
    { '#settings':      { handler: 'settingsBeforeShow', events: 'bs' } },
    { '#settings':      { handler: 'settingsShow', events: 's' } },
    { '#newApp':        { handler: 'newAppBeforeShow', events: 'bs' } },
  ], r.controller);


  return r;
})(rskybox || {}, jQuery);

