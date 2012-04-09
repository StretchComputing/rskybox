var RSKYBOX = (function (r, $) {
  'use strict';


  r.controller = {
    // Applications
    applicationsInit: function () {
      try {
        r.log.info('entering', 'MainController.applicationsInit');
        r.applicationsView = new r.ApplicationsView({
          collection: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.applicationsInit');
      }
    },

    applicationsShow: function () {
      try {
        r.log.info('entering', 'MainController.applicationsShow');
        r.applicationsView.setElement($.mobile.activePage);
        r.session.getCollection(r.session.keys.applications, r.applicationsView.collection);
      } catch (e) {
        r.log.error(e, 'MainController.applicationsShow');
      }
    },


    // Application
    applicationInit: function () {
      try {
        r.log.info('entering', 'MainController.applicationInit');
        r.applicationView = new r.ApplicationView({
          model: new r.Application(),
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.applicationInit');
      }
    },

    applicationShow: function () {
      try {
        r.log.info('entering', 'MainController.applicationShow');
        r.applicationView.setElement($.mobile.activePage);
        r.session.getCollection(r.session.keys.applications, r.applicationView.options.applications);
      } catch (e) {
        r.log.error(e, 'MainController.applicationShow');
      }
    },


    // Settings
    settingsInit: function () {
      try {
        r.log.info('entering', 'MainController.settingsInit');
        r.settingsView = new r.SettingsView({
          model: new r.User(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.settingsInit');
      }
    },

    settingsShow: function () {
      try {
        r.log.info('entering', 'MainController.settingsShow');
        r.settingsView.setElement($.mobile.activePage);
        r.settingsView.model.clear({silent: true});
        r.settingsView.model.set({id: 'current'});
        r.settingsView.model.setUrl();
        r.session.getModel(r.session.keys.currentUser, r.settingsView.model);
      } catch (e) {
        r.log.error(e, 'MainController.settingsShow');
      }
    },


    // New App
    newAppBeforeShow: function () {
      try {
        r.log.info('entering', 'MainController.newBeforeShow');
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
      } catch (e) {
        r.log.error(e, 'MainController.newAppBeforeShow');
      }
    },

    newAppShow: function () {
      try {
        r.log.info('entering', 'MainController.newAppShow');
        r.newAppView.render();
      } catch (e) {
        r.log.error(e, 'MainController.newAppShow');
      }
    },


    // Feedback List
    feedbackListInit: function () {
      try {
        r.log.info('entering', 'MainController.feedbackListInit');
        r.feedbackList = new r.FeedbackList();
        r.feedbackListView = new r.FeedbackListView({
          collection: r.feedbackList,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.feedbackListInit');
      }
    },

    feedbackListShow: function () {
      try {
        r.log.info('entering', 'MainController.feedbackListShow');
        r.feedbackListView.setElement($.mobile.activePage);
        r.feedbackList.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.feedbackListView.options.applications);
        r.feedbackList.fetch({data: { status: r.session.params.status }});
        r.feedbackListView.renderArchiveButton('#feedbackList');
      } catch (e) {
        r.log.error(e, 'MainController.feedbackListShow');
      }
    },


    // Feedback
    feedbackInit: function () {
      try {
        r.log.info('entering', 'MainController.feedbackInit');
        r.feedback = new r.Feedback({});
        r.feedbackView = new r.FeedbackView({
          model: r.feedback,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.feedbackInit');
      }
    },

    feedbackShow: function () {
      try {
        r.log.info('entering', 'MainController.feedbackShow');
        r.feedbackView.setElement($.mobile.activePage);
        r.feedback.set({id: r.session.params.id}, {silent: true});
        r.feedback.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.feedbackView.options.applications);
        r.feedback.fetch({
          statusCode: r.statusCodeHandlers(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.feedbackShow');
      }
    },


    // Logs
    logsInit: function () {
      try {
        r.log.info('entering', 'MainController.logsInit');
        r.logs = new r.Logs();
        r.logsView = new r.LogsView({
          collection: r.logs,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.logsInit');
      }
    },

    logsShow: function () {
      try {
        r.log.info('entering', 'MainController.logsShow');
        r.logsView.setElement($.mobile.activePage);
        r.logs.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.logsView.options.applications);
        r.logs.fetch({data: { status: r.session.params.status }});
        r.logsView.renderArchiveButton('#logs');
      } catch (e) {
        r.log.error(e, 'MainController.logsShow');
      }
    },


    // Log
    logInit: function () {
      try {
        r.log.info('entering', 'MainController.logInit');
        r.logCurrent = new r.Log({});
        r.logView = new r.LogView({
          model: r.logCurrent,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.logInit');
      }
    },

    logShow: function () {
      try {
        r.log.info('entering', 'MainController.logShow');
        r.logView.setElement($.mobile.activePage);
        r.logCurrent.set({id: r.session.params.id}, {silent: true});
        r.logCurrent.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.logView.options.applications);
        r.logCurrent.fetch({
          statusCode: r.statusCodeHandlers(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.logShow');
      }
    },


    // Crashes
    crashesInit: function () {
      try {
        r.log.info('entering', 'MainController.crashesInit');
        r.crashes = new r.Crashes();
        r.crashesView = new r.CrashesView({
          collection: r.crashes,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.crashesInit');
      }
    },

    crashesShow: function () {
      try {
        r.log.info('entering', 'MainController.crashesShow');
        r.crashesView.setElement($.mobile.activePage);
        r.crashes.setAppUrl(r.session.params.appId);
        r.crashes.fetch({data: { status: r.session.params.status }});
        r.session.getCollection(r.session.keys.applications, r.crashesView.options.applications);
        r.crashesView.renderArchiveButton('#crashes');
      } catch (e) {
        r.log.error(e, 'MainController.crashesShow');
      }
    },


    // Crash
    crashInit: function () {
      try {
        r.log.info('entering', 'MainController.crashInit');
        r.crash = new r.Crash({});
        r.crashView = new r.CrashView({
          model: r.crash,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.crashInit');
      }
    },

    crashShow: function () {
      try {
        r.log.info('entering', 'MainController.crashShow');
        r.crashView.setElement($.mobile.activePage);
        r.crash.set({id: r.session.params.id}, {silent: true});
        r.crash.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.crashView.options.applications);
        r.crash.fetch({
          statusCode: r.statusCodeHandlers(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.crashShow');
      }
    },


    // Members
    membersInit: function () {
      try {
        r.log.info('entering', 'MainController.membersInit');
        r.members = new r.Members();
        r.membersView = new r.MembersView({
          collection: r.members,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.membersInit');
      }
    },

    membersShow: function () {
      try {
        r.log.info('entering', 'MainController.membersShow');
        r.membersView.setElement($.mobile.activePage);
        r.members.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.membersView.options.applications);
        r.members.fetch({
          statusCode: r.statusCodeHandlers(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.membersShow');
      }
    },


    // Member
    memberInit: function () {
      try {
        r.log.info('entering', 'MainController.memberInit');
        r.member = new r.Member();
        r.memberView = new r.MemberView({
          model: r.member,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.memberInit');
      }
    },

    memberShow: function () {
      try {
        r.log.info('entering', 'MainController.memberShow');
        r.memberView.setElement($.mobile.activePage);
        r.member.set({id: r.session.params.id}, {silent: true});
        r.member.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.memberView.options.applications);
        r.member.fetch({
          statusCode: r.statusCodeHandlers(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.memberShow');
      }
    },


    // New Member
    newMemberBeforeShow: function () {
      try {
        r.log.info('entering', 'MainController.newMemberBeforeShow');
        if (r.newMember) { delete r.newMember; }
        if (r.newMemberView) {
          r.newMemberView.undelegateEvents();
          delete r.newMemberView;
        }
        r.newMember = new r.Member();
        r.newMember.setAppUrl(r.session.params.appId);
        r.newMemberView = new r.NewMemberView({
          el: $.mobile.activePage,
          model: r.newMember,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.newMemberBeforeShow');
      }
    },

    newMemberShow: function () {
      try {
        r.log.info('entering', 'MainController.newMemberShow');
        r.session.getCollection(r.session.keys.applications, r.newMemberView.options.applications);
        r.newMemberView.render();
      } catch (e) {
        r.log.error(e, 'MainController.newMemberShow');
      }
    },


    // Endusers
    endusersInit: function () {
      try {
        r.log.info('entering', 'MainController.endusersInit');
        r.endusers = new r.Endusers();
        r.endusersView = new r.EndusersView({
          collection: r.endusers,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.endusersInit');
      }
    },

    endusersShow: function () {
      try {
        r.log.info('entering', 'MainController.endusersShow');
        r.endusersView.setElement($.mobile.activePage);
        r.endusers.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.endusersView.options.applications);
        r.endusers.fetch({
          statusCode: r.statusCodeHandlers(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.endusersShow');
      }
    },


    // Enduser
    enduserInit: function () {
      try {
        r.log.info('entering', 'MainController.enduserInit');
        r.enduser = new r.Enduser({});
        r.enduserView = new r.EnduserView({
          model: r.enduser,
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.enduserInit');
      }
    },

    enduserShow: function () {
      try {
        r.log.info('entering', 'MainController.enduserShow');
        r.enduserView.setElement($.mobile.activePage);
        r.enduser.set({id: r.session.params.id}, {silent: true});
        r.enduser.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.enduserView.options.applications);
        r.enduser.fetch({
          statusCode: r.statusCodeHandlers(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.enduserShow');
      }
    },


    // Session Setup
    setupSession: function (eventType, matchObj, ui, page, evt) {
      try {
        r.log.info('entering', 'MainController.setupSession');
        r.session = r.session || {};
        r.session.params = r.router.getParams(location.hash);
      } catch (e) {
        r.log.error(e, 'MainController.setupSession');
      }
    },
  };


  $(document).bind('pagebeforechange', function (evt, data) {
    try {
      if (!r.isLoggedIn()) {
        r.log.debug('not logged in', 'pagebeforechange');
        r.flash.set('warning', 'Login required');
        evt.preventDefault();
        r.changePage('login', 'signup');
        return;
      }
    } catch (e) {
      r.log.error(e, 'RSKYBOX.main.pagebeforechange');
    }
  });


  try {
    r.router = new $.mobile.Router([
      { '.*':                       { handler: 'setupSession',        events: 'bs'  } },
      { '#applications':            { handler: 'applicationsInit',    events: 'i'   } },
      { '#applications':            { handler: 'applicationsShow',    events: 's'   } },
      { '#application[?]appId=.*':  { handler: 'applicationInit',     events: 'i'   } },
      { '#application[?]appId=.*':  { handler: 'applicationShow',     events: 's'   } },
      { '#settings':                { handler: 'settingsInit',        events: 'i'   } },
      { '#settings':                { handler: 'settingsShow',        events: 's'   } },
      { '#newApp':                  { handler: 'newAppBeforeShow',    events: 'bs'  } },
      { '#newApp':                  { handler: 'newAppShow',          events: 's'   } },
      { '#feedbackList':            { handler: 'feedbackListInit',    events: 'i'   } },
      { '#feedbackList':            { handler: 'feedbackListShow',    events: 's'   } },
      { '#feedback[?]id=.*':        { handler: 'feedbackInit',        events: 'i'   } },
      { '#feedback[?]id=.*':        { handler: 'feedbackShow',        events: 's'   } },
      { '#logs':                    { handler: 'logsInit',            events: 'i'   } },
      { '#logs':                    { handler: 'logsShow',            events: 's'   } },
      { '#log[?]id=.*':             { handler: 'logInit',             events: 'i'   } },
      { '#log[?]id=.*':             { handler: 'logShow',             events: 's'   } },
      { '#crashes':                 { handler: 'crashesInit',         events: 'i'   } },
      { '#crashes':                 { handler: 'crashesShow',         events: 's'   } },
      { '#crash[?]id=.*':           { handler: 'crashInit',           events: 'i'   } },
      { '#crash[?]id=.*':           { handler: 'crashShow',           events: 's'   } },
      { '#members':                 { handler: 'membersInit',         events: 'i'   } },
      { '#members':                 { handler: 'membersShow',         events: 's'   } },
      { '#member[?]id=.*':          { handler: 'memberInit',          events: 'i'   } },
      { '#member[?]id=.*':          { handler: 'memberShow',          events: 's'   } },
      { '#newMember':               { handler: 'newMemberBeforeShow', events: 'bs'  } },
      { '#newMember':               { handler: 'newMemberShow',       events: 's'   } },
      { '#endusers':                { handler: 'endusersInit',        events: 'i'   } },
      { '#endusers':                { handler: 'endusersShow',        events: 's'   } },
      { '#enduser[?]id=.*':         { handler: 'enduserInit',         events: 'i'   } },
      { '#enduser[?]id=.*':         { handler: 'enduserShow',         events: 's'   } },
    ], r.controller);
  } catch (e) {
    r.log.error(e, 'RSKYBOX.main.router');
  }


  return r;
}(RSKYBOX || {}, jQuery));
