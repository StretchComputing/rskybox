var RSKYBOX = (function (r, $) {
  'use strict';


  r.controller = {
    // Applications
    applicationsInit: function () {
      r.log.debug('entering', 'MainController.applicationsInit');
      r.applicationsView = new r.ApplicationsView({
        collection: r.getApplications(),
      });
    },

    applicationsShow: function () {
      r.log.debug('entering', 'MainController.applicationsShow');
      r.applicationsView.setElement($.mobile.activePage);
      r.getApplications();
      r.applicationsView.render();
    },


    // Application
    applicationInit: function () {
      r.log.debug('entering', 'MainController.applicationInit');
      r.application = new r.Application({});
      r.applicationView = new r.ApplicationView({
        model: r.application,
      });
    },

    applicationShow: function () {
      r.log.debug('entering', 'MainController.applicationShow');
      r.applicationView.setElement($.mobile.activePage);
      r.application.set({id: r.session.params.id}, {silent: true});
      r.application.setUrl();
      r.application.fetch({
        statusCode: r.statusCodeHandlers(),
      });
    },


    // Settings
    settingsInit: function () {
      r.log.debug('entering', 'MainController.settingsInit');
      r.currentUser = new r.User({});
      r.settingsView = new r.SettingsView({
        model: r.currentUser,
      });
    },

    settingsShow: function () {
      r.log.debug('entering', 'MainController.settingsShow');
      r.settingsView.setElement($.mobile.activePage);
      r.currentUser.set({id: 'current'});
      r.currentUser.setUrl();
      r.currentUser.fetch({
        statusCode: r.statusCodeHandlers(),
      });
    },


    // New App
    newAppBeforeShow: function () {
      r.log.debug('entering', 'MainController.newBeforeShow');
      if (r.newApp) { delete r.newApp; }
      if (r.newAppView) {
        r.newAppView.undelegateEvents();
        delete r.newAppView;
      }
      r.newApp = new r.Application();
      r.newAppView = new r.NewApplicationView({
        el: $('#newAppForm'),
        model: r.newApp,
      });
    },

    newAppShow: function () {
      r.log.debug('entering', 'MainController.newAppShow');
      r.newAppView.render();
    },


    // Feedback List
    feedbackListInit: function () {
      r.log.debug('entering', 'MainController.feedbackListInit');
      r.feedbackList = new r.FeedbackList();
      r.feedbackListView = new r.FeedbackListView({
        collection: r.feedbackList,
      });
    },

    feedbackListShow: function () {
      r.log.debug('entering', 'MainController.feedbackListShow');
      r.feedbackListView.setElement($.mobile.activePage);
      r.feedbackList.setAppUrl(r.session.params.id);
      r.feedbackList.fetch({data: { status: r.session.params.status }});
      r.feedbackListView.renderArchiveButton('#feedbackList');
    },


    // Feedback
    feedbackInit: function () {
      r.log.debug('entering', 'MainController.feedbackInit');
      r.feedback = new r.Feedback({});
      r.feedbackView = new r.FeedbackView({
        model: r.feedback,
      });
    },

    feedbackShow: function () {
      r.log.debug('entering', 'MainController.feedbackShow');
      r.feedbackView.setElement($.mobile.activePage);
      r.feedback.set({id: r.session.params.id}, {silent: true});
      r.feedback.setAppUrl(r.session.params.appId);
      r.feedback.fetch({
        statusCode: r.statusCodeHandlers(),
      });
    },


    // Logs
    logsInit: function () {
      r.log.debug('entering', 'MainController.logsInit');
      r.logs = new r.Logs();
      r.logsView = new r.LogsView({
        collection: r.logs,
      });
    },

    logsShow: function () {
      r.log.debug('entering', 'MainController.logsShow');
      r.logsView.setElement($.mobile.activePage);
      r.logs.setAppUrl(r.session.params.id);
      r.logs.fetch({data: { status: r.session.params.status }});
      r.logsView.renderArchiveButton('#logs');
    },


    // Log
    logInit: function () {
      r.log.debug('entering', 'MainController.logInit');
      r.logCurrent = new r.Log({});
      r.logView = new r.LogView({
        model: r.logCurrent,
      });
    },

    logShow: function () {
      r.log.debug('entering', 'MainController.logShow');
      r.logView.setElement($.mobile.activePage);
      r.logCurrent.set({id: r.session.params.id}, {silent: true});
      r.logCurrent.setAppUrl(r.session.params.appId);
      r.logCurrent.fetch({
        statusCode: r.statusCodeHandlers(),
      });
    },


    // Crashes
    crashesInit: function () {
      r.log.debug('entering', 'MainController.crashesInit');
      r.crashes = new r.Crashes();
      r.crashesView = new r.CrashesView({
        collection: r.crashes,
      });
    },

    crashesShow: function () {
      r.log.debug('entering', 'MainController.crashesShow');
      r.crashesView.setElement($.mobile.activePage);
      r.crashes.setAppUrl(r.session.params.id);
      r.crashes.fetch({data: { status: r.session.params.status }});
      r.crashesView.renderArchiveButton('#crashes');
    },


    // Crash
    crashInit: function () {
      r.log.debug('entering', 'MainController.crashInit');
      r.crash = new r.Crash({});
      r.crashView = new r.CrashView({
        model: r.crash,
      });
    },

    crashShow: function () {
      r.log.debug('entering', 'MainController.crashShow');
      r.crashView.setElement($.mobile.activePage);
      r.crash.set({id: r.session.params.id}, {silent: true});
      r.crash.setAppUrl(r.session.params.appId);
      r.crash.fetch({
        statusCode: r.statusCodeHandlers(),
      });
    },


    // Members
    membersInit: function () {
      r.log.debug('entering', 'MainController.membersInit');
      r.members = new r.Members();
      r.membersView = new r.MembersView({
        collection: r.members,
      });
    },

    membersShow: function () {
      r.log.debug('entering', 'MainController.membersShow');
      r.membersView.setElement($.mobile.activePage);
      r.members.setAppUrl(r.session.params.id);
      r.members.fetch({
        statusCode: r.statusCodeHandlers(),
      });
    },


    // Member
    memberInit: function () {
      r.log.debug('entering', 'MainController.memberInit');
      r.member = new r.Member();
      r.memberView = new r.MemberView({
        model: r.member,
      });
    },

    memberShow: function () {
      r.log.debug('entering', 'MainController.memberShow');
      r.memberView.setElement($.mobile.activePage);
      r.member.set({id: r.session.params.id}, {silent: true});
      r.member.setAppUrl(r.session.params.appId);
      r.member.fetch({
        statusCode: r.statusCodeHandlers(),
      });
    },


    // New Member
    newMemberBeforeShow: function () {
      r.log.debug('entering', 'MainController.newMemberBeforeShow');
      if (r.newMember) { delete r.newMember; }
      if (r.newMemberView) {
        r.newMemberView.undelegateEvents();
        delete r.newMemberView;
      }
      r.newMember = new r.Member();
      r.newMember.setAppUrl(r.session.params.id);
      r.newMemberView = new r.NewMemberView({
        el: $('#newMemberForm'),
        model: r.newMember,
      });
    },

    newMemberShow: function () {
      r.log.debug('entering', 'MainController.newMemberShow');
      r.newMemberView.render();
    },


    // Endusers
    endusersInit: function () {
      r.log.debug('entering', 'MainController.endusersInit');
      r.endusers = new r.Endusers();
      r.endusersView = new r.EndusersView({
        collection: r.endusers,
      });
    },

    endusersShow: function () {
      r.log.debug('entering', 'MainController.endusersShow');
      r.endusersView.setElement($.mobile.activePage);
      r.endusers.setAppUrl(r.session.params.id);
      r.endusers.fetch({
        statusCode: r.statusCodeHandlers(),
      });
    },


    // Enduser
    enduserInit: function () {
      r.log.debug('entering', 'MainController.enduserInit');
      r.enduser = new r.Enduser({});
      r.enduserView = new r.EnduserView({
        model: r.enduser,
      });
    },

    enduserShow: function () {
      r.log.debug('entering', 'MainController.enduserShow');
      r.enduserView.setElement($.mobile.activePage);
      r.enduser.set({id: r.session.params.id}, {silent: true});
      r.enduser.setAppUrl(r.session.params.appId);
      r.enduser.fetch({
        statusCode: r.statusCodeHandlers(),
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
    { '.*':                   { handler: 'setupSession',      events: 'bs'  } },
    { '#applications':        { handler: 'applicationsInit',  events: 'i'   } },
    { '#applications':        { handler: 'applicationsShow',  events: 's'   } },
    { '#application[?]id=.*': { handler: 'applicationInit',   events: 'i'   } },
    { '#application[?]id=.*': { handler: 'applicationShow',   events: 's'   } },
    { '#settings':            { handler: 'settingsInit',      events: 'i'   } },
    { '#settings':            { handler: 'settingsShow',      events: 's'   } },
    { '#newApp':              { handler: 'newAppBeforeShow',  events: 'bs'  } },
    { '#newApp':              { handler: 'newAppShow',        events: 's'   } },
    { '#feedbackList':        { handler: 'feedbackListInit',  events: 'i'   } },
    { '#feedbackList':        { handler: 'feedbackListShow',  events: 's'   } },
    { '#feedback[?]id=.*':    { handler: 'feedbackInit',      events: 'i'   } },
    { '#feedback[?]id=.*':    { handler: 'feedbackShow',      events: 's'   } },
    { '#logs':                { handler: 'logsInit',          events: 'i'   } },
    { '#logs':                { handler: 'logsShow',          events: 's'   } },
    { '#log[?]id=.*':         { handler: 'logInit',           events: 'i'   } },
    { '#log[?]id=.*':         { handler: 'logShow',           events: 's'   } },
    { '#crashes':             { handler: 'crashesInit',       events: 'i'   } },
    { '#crashes':             { handler: 'crashesShow',       events: 's'   } },
    { '#crash[?]id=.*':       { handler: 'crashInit',         events: 'i'   } },
    { '#crash[?]id=.*':       { handler: 'crashShow',         events: 's'   } },
    { '#members':             { handler: 'membersInit',       events: 'i'   } },
    { '#members':             { handler: 'membersShow',       events: 's'   } },
    { '#member[?]id=.*':      { handler: 'memberInit',        events: 'i'   } },
    { '#member[?]id=.*':      { handler: 'memberShow',        events: 's'   } },
    { '#newMember':           { handler: 'newMemberBeforeShow', events: 'bs' } },
    { '#newMember':           { handler: 'newMemberShow',     events: 's'   } },
    { '#endusers':            { handler: 'endusersInit',      events: 'i'   } },
    { '#endusers':            { handler: 'endusersShow',      events: 's'   } },
    { '#enduser[?]id=.*':     { handler: 'enduserInit',       events: 'i'   } },
    { '#enduser[?]id=.*':     { handler: 'enduserShow',       events: 's'   } },
  ], r.controller);


  return r;
}(RSKYBOX || {}, jQuery));
