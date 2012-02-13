var RSKYBOX = (function (r, $) {
  'use strict';


  r.controller = {
    // Applications
    applicationsBeforeShow: function () {
      r.log.debug('applicationsBeforeShow');
      delete(r.applications);
      r.applications = new r.Applications();
      r.applicationsView = new r.ApplicationsView({
        el: r.getContentDiv(),
        collection: r.applications
      });
    },

    applicationsShow: function () {
      r.log.debug('applicationsShow');
      r.applications.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Application
    applicationBeforeShow: function () {
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

    applicationShow: function () {
      r.log.debug('applicationShow');
      r.application.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Settings
    settingsCreate: function () {
      r.log.debug('settingsCreate');
      $('#logout').click(function () {
        r.log.debug('logout');
        r.unsetCookie();
        r.changePage('root', 'signup');
      });
    },

    settingsBeforeShow: function () {
      r.log.debug('settingsBeforeShow');
    },

    settingsShow: function () {
      r.log.debug('settingsShow');
    },


    // New App
    newAppBeforeShow: function () {
      r.log.debug('newAppBeforeShow');
      r.newApp = new r.Application();
      r.newAppView = new r.NewApplicationView({
        el: $('#newAppForm'),
        model: r.newApp
      });
    },

    newAppShow: function () {
      r.log.debug('newAppShow');
      r.newAppView.render();
    },


    // Feedback List
    feedbackListBeforeShow: function () {
      r.log.debug('feedbackListBeforeShow');
      delete(r.feedbackList);
      r.feedbackList = new r.FeedbackList();
      r.feedbackList.setAppUrl(r.getParameterByName(location.hash, 'id'));
      r.feedbackListView = new r.FeedbackListView({
        el: r.getContentDiv(),
        collection: r.feedbackList
      });
    },

    feedbackListShow: function () {
      r.log.debug('feedbackListShow');
      r.feedbackList.fetch();
    },


    // Feedback
    feedbackBeforeShow: function () {
      r.log.debug('feedbackBeforeShow');
      delete(r.feedback);
      r.feedback = new r.Feedback({
        id: r.getParameterByName(location.hash, 'id')
      });
      r.feedback.setAppUrl(r.getParameterByName(location.hash, 'appId'));
      r.feedbackView = new r.FeedbackView({
        el: r.getContentDiv(),
        model: r.feedback
      });
    },

    feedbackShow: function () {
      r.log.debug('feedbackShow');
      r.feedback.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Logs
    logsBeforeShow: function () {
      r.log.debug('logsBeforeShow');
      delete(r.logs);
      r.logs = new r.Logs();
      r.logs.setAppUrl(r.getParameterByName(location.hash, 'id'));
      r.logsView = new r.LogsView({
        el: r.getContentDiv(),
        collection: r.logs
      });
    },

    logsShow: function () {
      r.log.debug('logsShow');
      r.logs.fetch();
    },


    // Log
    logBeforeShow: function () {
      r.log.debug('logBeforeShow');
      delete(r.logCurrent);
      r.logCurrent = new r.Log({
        id: r.getParameterByName(location.hash, 'id')
      });
      r.logCurrent.setAppUrl(r.getParameterByName(location.hash, 'appId'));
      r.logView = new r.LogView({
        el: r.getContentDiv(),
        model: r.logCurrent
      });
    },

    logShow: function () {
      r.log.debug('logShow');
      r.logCurrent.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Crashes
    crashesBeforeShow: function () {
      r.log.debug('crashesBeforeShow');
      delete(r.crashes);
      r.crashes = new r.Crashes();
      r.crashes.setAppUrl(r.getParameterByName(location.hash, 'id'));
      r.crashesView = new r.CrashesView({
        el: r.getContentDiv(),
        collection: r.crashes
      });
    },

    crashesShow: function () {
      r.log.debug('crashesShow');
      r.crashes.fetch();
    },


    // Crash
    crashBeforeShow: function () {
      r.log.debug('crashBeforeShow');
      delete(r.crash);
      r.crash = new r.Crash({
        id: r.getParameterByName(location.hash, 'id')
      });
      r.crash.setAppUrl(r.getParameterByName(location.hash, 'appId'));
      r.crashView = new r.CrashView({
        el: r.getContentDiv(),
        model: r.crash
      });
    },

    crashShow: function () {
      r.log.debug('crashShow');
      r.crash.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Members
    membersBeforeShow: function () {
      r.log.debug('membersBeforeShow');
    },

    membersShow: function () {
      r.log.debug('membersShow');
    },


    // End Users
    endusersBeforeShow: function () {
      r.log.debug('endusersBeforeShow');
    },

    endusersShow: function () {
      r.log.debug('endusersShow');
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
    { '#feedbackList':  { handler: 'feedbackListBeforeShow', events: 'bs' } },
    { '#feedbackList':  { handler: 'feedbackListShow', events: 's' } },
    { '#feedback[?]id=.*':      { handler: 'feedbackBeforeShow', events: 'bs' } },
    { '#feedback[?]id=.*':      { handler: 'feedbackShow', events: 's' } },
    { '#logs':          { handler: 'logsBeforeShow', events: 'bs' } },
    { '#logs':          { handler: 'logsShow', events: 's' } },
    { '#log[?]id=.*':   { handler: 'logBeforeShow', events: 'bs' } },
    { '#log[?]id=.*':   { handler: 'logShow', events: 's' } },
    { '#crashes':       { handler: 'crashesBeforeShow', events: 'bs' } },
    { '#crashes':       { handler: 'crashesShow', events: 's' } },
    { '#crash[?]id=.*': { handler: 'crashBeforeShow', events: 'bs' } },
    { '#crash[?]id=.*': { handler: 'crashShow', events: 's' } },
    { '#members':       { handler: 'membersBeforeShow', events: 'bs' } },
    { '#members':       { handler: 'membersShow', events: 's' } },
    { '#endusers':      { handler: 'endusersBeforeShow', events: 'bs' } },
    { '#endusers':      { handler: 'endusersShow', events: 's' } },
  ], r.controller);


  return r;
}(RSKYBOX || {}, jQuery));
