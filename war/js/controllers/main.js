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

    applicationBeforeShow: function() {
      r.log.debug('applicationBeforeShow');
      delete(r.application);
      r.application = new r.Application({
        id: r.getParameterByName(location.hash, 'id')
      });
      r.applicationView = new r.ApplicationView({
        el: r.getContentDiv(),
        model: r.application
      });
    },

    applicationShow: function() {
      r.log.debug('applicationShow');
      r.application.fetch();
    },

    settingsCreate: function() {
      r.log.debug('settingsCreate');
      $('#logout').click(function() {
        r.log.debug('logout');
        r.unsetCookie();
        r.changePage('root', 'signup');
      });
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
      r.newAppView = new r.NewApplicationView({
        el: $('#newAppForm'),
        model: r.newApp
      });
    },

    newAppShow: function() {
      r.log.debug('newAppShow');
      r.newAppView.render();
    },

  };

  r.router = new $.mobile.Router([
    { '#applications':  { handler: 'applicationsBeforeShow', events: 'bs' } },
    { '#applications':  { handler: 'applicationsShow', events: 's' } },
    { '#application[?]id=.*':   { handler: 'applicationBeforeShow', events: 'bs' } },
    { '#application[?]id=.*':   { handler: 'applicationShow', events: 's' } },
    { '#settings':      { handler: 'settingsCreate', events: 'c' } },
    { '#settings':      { handler: 'settingsBeforeShow', events: 'bs' } },
    { '#settings':      { handler: 'settingsShow', events: 's' } },
    { '#newApp':        { handler: 'newAppBeforeShow', events: 'bs' } },
    { '#newApp':        { handler: 'newAppShow', events: 's' } },
  ], r.controller);


  return r;
}(rskybox || {}, jQuery));

