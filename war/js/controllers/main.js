var RSKYBOX = (function (r, $) {
  'use strict';


  r.controller = {
    // Applications
    applicationsBeforeShow: function () {
      r.log.debug('applicationsBeforeShow');
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
      r.feedbackList = new r.FeedbackList();
      r.feedbackList.setAppUrl(r.session.params.id);
      r.feedbackListView = new r.FeedbackListView({
        el: $.mobile.activePage,
        collection: r.feedbackList
      });
    },

    feedbackListShow: function () {
      r.log.debug('feedbackListShow');
      r.feedbackList.fetch({data: { status: r.session.params.status }});
    },


    // Feedback
    feedbackBeforeShow: function () {
      r.log.debug('feedbackBeforeShow');
      r.feedback = new r.Feedback({
        id: r.getParameterByName(location.hash, 'id')
      });
      r.feedback.setAppUrl(r.session.params.appId);
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
      r.members = new r.Members();
      r.members.setAppUrl(r.getParameterByName(location.hash, 'id'));
      r.membersView = new r.MembersView({
        el: r.getContentDiv(),
        collection: r.members
      });
    },

    membersShow: function () {
      r.log.debug('membersShow');
      r.members.fetch();
    },


    // Member
    memberBeforeShow: function () {
      r.log.debug('memberBeforeShow');
      r.member = new r.Member({
        id: r.getParameterByName(location.hash, 'id')
      });
      r.member.setAppUrl(r.getParameterByName(location.hash, 'appId'));
      r.memberView = new r.MemberView({
        el: r.getContentDiv(),
        model: r.member
      });
    },

    memberShow: function () {
      r.log.debug('memberShow');
      r.member.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Endusers
    endusersBeforeShow: function () {
      r.log.debug('endusersBeforeShow');
      r.endusers = new r.Endusers();
      r.endusers.setAppUrl(r.getParameterByName(location.hash, 'id'));
      r.endusersView = new r.EndusersView({
        el: r.getContentDiv(),
        collection: r.endusers
      });
    },

    endusersShow: function () {
      r.log.debug('endusersShow');
      r.endusers.fetch();
    },


    // Enduser
    enduserBeforeShow: function () {
      r.log.debug('enduserBeforeShow');
      r.enduser = new r.Enduser({
        id: r.getParameterByName(location.hash, 'id')
      });
      r.enduser.setAppUrl(r.getParameterByName(location.hash, 'appId'));
      r.enduserView = new r.EnduserView({
        el: r.getContentDiv(),
        model: r.enduser
      });
    },

    enduserShow: function () {
      r.log.debug('enduserShow');
      r.enduser.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },

    testHandler: function (eventType, matchObj, ui, page, evt) {
      r.log.debug('testHandler');
      console.log(eventType, matchObj, ui, page, evt);
    },

    setupSession: function (eventType, matchObj, ui, page, evt) {
      r.log.debug('setupSession');
      r.session = {};
      r.session.params = r.router.getParams(location.hash);
    },
  };

  r.router = new $.mobile.Router([
    { '.*':             { handler: 'setupSession', events: 'bs' } },
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
    { '#member[?]id=.*':        { handler: 'memberBeforeShow', events: 'bs' } },
    { '#member[?]id=.*':        { handler: 'memberShow', events: 's' } },
    { '#endusers':      { handler: 'endusersBeforeShow', events: 'bs' } },
    { '#endusers':      { handler: 'endusersShow', events: 's' } },
    { '#enduser[?]id=.*':       { handler: 'enduserBeforeShow', events: 'bs' } },
    { '#enduser[?]id=.*':       { handler: 'enduserShow', events: 's' } },
    { '#test': { handler: 'testHandler', events: 'bc,c,i,bs,s,bh,h,rm' } },
  ], r.controller);


  return r;
}(RSKYBOX || {}, jQuery));
