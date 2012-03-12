var RSKYBOX = (function (r, $) {
  'use strict';


  r.controller = {
    // Applications
    applicationsBeforeShow: function () {
      r.log.debug('entering', 'MainController.applicationsBeforeShow');
      r.applications = new r.Applications();
      r.applicationsView = new r.ApplicationsView({
        el: $.mobile.activePage,
        collection: r.applications
      });
    },

    applicationsShow: function () {
      r.log.debug('entering', 'MainController.applicationsShow');
      r.applications.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Application
    applicationBeforeShow: function () {
      r.log.debug('entering', 'MainController.applicationBeforeShow');
      r.application = new r.Application({
        id: r.session.params.id
      });
      r.applicationView = new r.ApplicationView({
        el: $.mobile.activePage,
        model: r.application
      });
    },

    applicationShow: function () {
      r.log.debug('entering', 'MainController.applicationShow');
      r.application.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Settings
    settingsBeforeShow: function () {
      r.log.debug('entering', 'MainController.settingsBeforeShow');
      r.currentUser = new r.User({
        id: 'current'
      });
      r.settingsView = new r.SettingsView({
        el: $.mobile.activePage,
        model: r.currentUser
      });
    },

    settingsShow: function () {
      r.log.debug('entering', 'MainController.settingsShow');
      r.currentUser.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // New App
    newAppBeforeShow: function () {
      r.log.debug('entering', 'MainController.newAppBeforeShow');
      r.newApp = new r.Application();
      r.newAppView = new r.NewApplicationView({
        el: $('#newAppForm'),
        model: r.newApp
      });
    },

    newAppShow: function () {
      r.log.debug('entering', 'MainController.newAppShow');
      r.newAppView.render();
    },


    // Feedback List
    feedbackListBeforeShow: function () {
      r.log.debug('entering', 'MainController.feedbackListBeforeShow');
      r.feedbackList = new r.FeedbackList();
      r.feedbackList.setAppUrl(r.session.params.id);
      r.feedbackListView = new r.FeedbackListView({
        el: $.mobile.activePage,
        collection: r.feedbackList
      });
      r.feedbackListView.renderArchiveButton('#feedbackList');
    },

    feedbackListShow: function () {
      r.log.debug('entering', 'MainController.feedbackListShow');
      r.feedbackList.fetch({data: { status: r.session.params.status }});
    },


    // Feedback
    feedbackBeforeShow: function () {
      r.log.debug('entering', 'MainController.feedbackBeforeShow');
      r.feedback = new r.Feedback({
        id: r.session.params.id
      });
      r.feedback.setAppUrl(r.session.params.appId);
      r.feedbackView = new r.FeedbackView({
        el: $.mobile.activePage,
        model: r.feedback
      });
    },

    feedbackShow: function () {
      r.log.debug('entering', 'MainController.feedbackShow');
      r.feedback.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Logs
    logsBeforeShow: function () {
      r.log.debug('entering', 'MainController.logsBeforeShow');
      r.logs = new r.Logs();
      r.logs.setAppUrl(r.session.params.id);
      r.logsView = new r.LogsView({
        el: $.mobile.activePage,
        collection: r.logs
      });
      r.logsView.renderArchiveButton('#logs');
    },

    logsShow: function () {
      r.log.debug('entering', 'MainController.logsShow');
      r.logs.fetch({data: { status: r.session.params.status }});
    },


    // Log
    logBeforeShow: function () {
      r.log.debug('entering', 'MainController.logBeforeShow');
      r.logCurrent = new r.Log({
        id: r.session.params.id
      });
      r.logCurrent.setAppUrl(r.session.params.appId);
      r.logView = new r.LogView({
        el: $.mobile.activePage,
        model: r.logCurrent
      });
    },

    logShow: function () {
      r.log.debug('entering', 'MainController.logShow');
      r.logCurrent.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Crashes
    crashesBeforeShow: function () {
      r.log.debug('entering', 'MainController.crashesBeforeShow');
      r.crashes = new r.Crashes();
      r.crashes.setAppUrl(r.session.params.id);
      r.crashesView = new r.CrashesView({
        el: $.mobile.activePage,
        collection: r.crashes
      });
      r.crashesView.renderArchiveButton('#crashes');
    },

    crashesShow: function () {
      r.log.debug('entering', 'MainController.crashesShow');
      r.crashes.fetch({data: { status: r.session.params.status }});
    },


    // Crash
    crashBeforeShow: function () {
      r.log.debug('entering', 'MainController.crashBeforeShow');
      r.crash = new r.Crash({
        id: r.session.params.id
      });
      r.crash.setAppUrl(r.session.params.appId);
      r.crashView = new r.CrashView({
        el: $.mobile.activePage,
        model: r.crash
      });
    },

    crashShow: function () {
      r.log.debug('entering', 'MainController.crashShow');
      r.crash.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Members
    membersBeforeShow: function () {
      r.log.debug('entering', 'MainController.membersBeforeShow');
      r.members = new r.Members();
      r.members.setAppUrl(r.session.params.id);
      r.membersView = new r.MembersView({
        el: $.mobile.activePage,
        collection: r.members
      });
    },

    membersShow: function () {
      r.log.debug('entering', 'MainController.membersShow');
      r.members.fetch();
    },


    // Member
    memberBeforeShow: function () {
      r.log.debug('entering', 'MainController.memberBeforeShow');
      r.member = new r.Member({
        id: r.session.params.id
      });
      r.member.setAppUrl(r.session.params.appId);
      r.memberView = new r.MemberView({
        el: $.mobile.activePage,
        model: r.member
      });
    },

    memberShow: function () {
      r.log.debug('entering', 'MainController.memberShow');
      r.member.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // New Member
    newMemberBeforeShow: function () {
      r.log.debug('entering', 'MainController.newMemberBeforeShow');
      r.newMember = new r.Member();
      r.newMember.setAppUrl(r.session.params.id);
      r.newMemberView = new r.NewMemberView({
        el: $('#newMemberForm'),
        model: r.newMember
      });
    },

    newMemberShow: function () {
      r.log.debug('entering', 'MainController.newMemberShow');
      r.newMemberView.render();
    },


    // Endusers
    endusersBeforeShow: function () {
      r.log.debug('entering', 'MainController.endusersBeforeShow');
      r.endusers = new r.Endusers();
      r.endusers.setAppUrl(r.session.params.id);
      r.endusersView = new r.EndusersView({
        el: $.mobile.activePage,
        collection: r.endusers
      });
    },

    endusersShow: function () {
      r.log.debug('entering', 'MainController.endusersShow');
      r.endusers.fetch();
    },


    // Enduser
    enduserBeforeShow: function () {
      r.log.debug('entering', 'MainController.enduserBeforeShow');
      r.enduser = new r.Enduser({
        id: r.session.params.id
      });
      r.enduser.setAppUrl(r.session.params.appId);
      r.enduserView = new r.EnduserView({
        el: $.mobile.activePage,
        model: r.enduser
      });
    },

    enduserShow: function () {
      r.log.debug('entering', 'MainController.enduserShow');
      r.enduser.fetch({
        statusCode: r.statusCodeHandlers()
      });
    },


    // Session Setup
    setupSession: function (eventType, matchObj, ui, page, evt) {
      r.log.debug('entering', 'MainController.setupSession');
      r.session = {};
      r.session.params = r.router.getParams(location.hash);
    },
  };

  r.router = new $.mobile.Router([
    { '.*':                   { handler: 'setupSession',            events: 'bs' } },
    { '#applications':        { handler: 'applicationsBeforeShow',  events: 'bs' } },
    { '#applications':        { handler: 'applicationsShow',        events: 's'  } },
    { '#application[?]id=.*': { handler: 'applicationBeforeShow',   events: 'bs' } },
    { '#application[?]id=.*': { handler: 'applicationShow',         events: 's'  } },
    { '#settings':            { handler: 'settingsBeforeShow',      events: 'bs' } },
    { '#settings':            { handler: 'settingsShow',            events: 's'  } },
    { '#newApp':              { handler: 'newAppBeforeShow',        events: 'bs' } },
    { '#newApp':              { handler: 'newAppShow',              events: 's'  } },
    { '#feedbackList':        { handler: 'feedbackListBeforeShow',  events: 'bs' } },
    { '#feedbackList':        { handler: 'feedbackListShow',        events: 's'  } },
    { '#feedback[?]id=.*':    { handler: 'feedbackBeforeShow',      events: 'bs' } },
    { '#feedback[?]id=.*':    { handler: 'feedbackShow',            events: 's'  } },
    { '#logs':                { handler: 'logsBeforeShow',          events: 'bs' } },
    { '#logs':                { handler: 'logsShow',                events: 's'  } },
    { '#log[?]id=.*':         { handler: 'logBeforeShow',           events: 'bs' } },
    { '#log[?]id=.*':         { handler: 'logShow',                 events: 's'  } },
    { '#crashes':             { handler: 'crashesBeforeShow',       events: 'bs' } },
    { '#crashes':             { handler: 'crashesShow',             events: 's'  } },
    { '#crash[?]id=.*':       { handler: 'crashBeforeShow',         events: 'bs' } },
    { '#crash[?]id=.*':       { handler: 'crashShow',               events: 's'  } },
    { '#members':             { handler: 'membersBeforeShow',       events: 'bs' } },
    { '#members':             { handler: 'membersShow',             events: 's'  } },
    { '#member[?]id=.*':      { handler: 'memberBeforeShow',        events: 'bs' } },
    { '#member[?]id=.*':      { handler: 'memberShow',              events: 's'  } },
    { '#newMember':           { handler: 'newMemberBeforeShow',     events: 'bs' } },
    { '#newMember':           { handler: 'newMemberShow',           events: 's'  } },
    { '#endusers':            { handler: 'endusersBeforeShow',      events: 'bs' } },
    { '#endusers':            { handler: 'endusersShow',            events: 's'  } },
    { '#enduser[?]id=.*':     { handler: 'enduserBeforeShow',       events: 'bs' } },
    { '#enduser[?]id=.*':     { handler: 'enduserShow',             events: 's'  } },
  ], r.controller);


  return r;
}(RSKYBOX || {}, jQuery));
