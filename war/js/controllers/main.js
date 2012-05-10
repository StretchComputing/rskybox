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
        r.applicationView.model.clear({silent: true});
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
        if (r.newAppView) {
          r.newAppView.undelegateEvents();
          delete r.newAppView;
        }
        r.newAppView = new r.NewApplicationView({
          el: $('#newAppForm'),
          model: new r.Application(),
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
        r.feedbackListView = new r.FeedbackListView({
          collection: new r.FeedbackList(),
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
        r.feedbackListView.collection.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.feedbackListView.options.applications);
        r.feedbackListView.collection.fetch({data: { status: r.session.params.status }});
        r.feedbackListView.renderStatusButton('#feedbackList');
      } catch (e) {
        r.log.error(e, 'MainController.feedbackListShow');
      }
    },


    // Feedback
    feedbackInit: function () {
      try {
        r.log.info('entering', 'MainController.feedbackInit');
        r.feedbackView = new r.FeedbackView({
          model: new r.Feedback(),
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
        r.feedbackView.model.clear({silent: true});
        r.feedbackView.model.set({id: r.session.params.id}, {silent: true});
        r.feedbackView.model.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.feedbackView.options.applications);
        r.feedbackView.model.fetch({
          statusCode: r.statusCodeHandlers(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.feedbackShow');
      }
    },


    // iLogs
    iLogsInit: function () {
      try {
        r.log.info('entering', 'MainController.iLogsInit');
        r.logsView = new r.iLogsView({
          collection: new r.Incidents(),
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.iLogsInit');
      }
    },

    iLogsShow: function () {
      try {
        r.log.info('entering', 'MainController.iLogsShow');
        r.logsView.setElement($.mobile.activePage);
        r.logsView.collection.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.logsView.options.applications);
        r.logsView.collection.fetch({
          data: {
            tag: 'log',
            status: r.session.params.status
          },
        });
        r.logsView.renderStatusButton('#iLogs');
      } catch (e) {
        r.log.error(e, 'MainController.iLogsShow');
      }
    },


    // iLog
    iLogInit: function () {
      try {
        r.log.info('entering', 'MainController.iLogInit');
        r.logView = new r.iLogView({
          model: new r.Incident(),
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.iLogInit');
      }
    },

    iLogShow: function () {
      try {
        r.log.info('entering', 'MainController.iLogShow');
        r.logView.setElement($.mobile.activePage);
        r.logView.model.clear({ silent: true });
        r.logView.model.set({id: r.session.params.id}, {silent: true});
        r.logView.model.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.logView.options.applications);
        r.logView.model.fetch({
          statusCode: r.statusCodeHandlers(),
          data: {
            includeEvents: true,
          },
        });
      } catch (e) {
        r.log.error(e, 'MainController.iLogShow');
      }
    },


    // iCrashes
    iCrashesInit: function () {
      try {
        r.log.info('entering', 'MainController.iCrashesInit');
        r.crashesView = new r.iCrashesView({
          collection: new r.Incidents(),
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.iCrashesInit');
      }
    },

    iCrashesShow: function () {
      try {
        r.log.info('entering', 'MainController.iCrashesShow');
        r.crashesView.setElement($.mobile.activePage);
        r.crashesView.collection.setAppUrl(r.session.params.appId);
        r.crashesView.collection.fetch({
          data: {
            tag: 'crash',
            status: r.session.params.status,
          },
        });
        r.session.getCollection(r.session.keys.applications, r.crashesView.options.applications);
        r.crashesView.renderStatusButton('#iCrashes');
      } catch (e) {
        r.log.error(e, 'MainController.iCrashesShow');
      }
    },


    // iCrash
    iCrashInit: function () {
      try {
        r.log.info('entering', 'MainController.iCrashInit');
        r.crashView = new r.iCrashView({
          model: new r.Incident(),
          applications: new r.Applications(),
        });
      } catch (e) {
        r.log.error(e, 'MainController.iCrashInit');
      }
    },

    iCrashShow: function () {
      try {
        r.log.info('entering', 'MainController.iCrashShow');
        r.crashView.setElement($.mobile.activePage);
        r.crashView.model.clear({silent: true});
        r.crashView.model.set({id: r.session.params.id}, {silent: true});
        r.crashView.model.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.crashView.options.applications);
        r.crashView.model.fetch({
          statusCode: r.statusCodeHandlers(),
          data: {
            includeEvents: true,
          },
        });
      } catch (e) {
        r.log.error(e, 'MainController.iCrashShow');
      }
    },


    // Members
    membersInit: function () {
      try {
        r.log.info('entering', 'MainController.membersInit');
        r.membersView = new r.MembersView({
          collection: new r.Members(),
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
        r.membersView.collection.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.membersView.options.applications);
        r.membersView.collection.fetch({
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
        r.memberView = new r.MemberView({
          model: new r.Member(),
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
        r.memberView.model.clear({silent: true});
        r.memberView.model.set({id: r.session.params.id}, {silent: true});
        r.memberView.model.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.memberView.options.applications);
        r.memberView.model.fetch({
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
        if (r.newMemberView) {
          r.newMemberView.undelegateEvents();
          delete r.newMemberView;
        }
        r.newMemberView = new r.NewMemberView({
          el: $.mobile.activePage,
          model: new r.Member(),
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
        r.newMemberView.model.setAppUrl(r.session.params.appId);
        r.newMemberView.render();
      } catch (e) {
        r.log.error(e, 'MainController.newMemberShow');
      }
    },


    // Endusers
    endusersInit: function () {
      try {
        r.log.info('entering', 'MainController.endusersInit');
        r.endusersView = new r.EndusersView({
          collection: new r.Endusers(),
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
        r.endusersView.collection.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.endusersView.options.applications);
        r.endusersView.collection.fetch({
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
        r.enduserView = new r.EnduserView({
          model: new r.Enduser(),
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
        r.enduserView.model.clear({silent: true});
        r.enduserView.model.set({id: r.session.params.id}, {silent: true});
        r.enduserView.model.setAppUrl(r.session.params.appId);
        r.session.getCollection(r.session.keys.applications, r.enduserView.options.applications);
        r.enduserView.model.fetch({
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
        r.log.info('not logged in', 'pagebeforechange');
        r.destination.set(location.pathname + location.hash);
        r.flash.set('warning', 'Login required');
        evt.preventDefault();
        r.changePage('login', 'signup');
        return;
      }
    } catch (e) {
      r.log.error(e, 'RSKYBOX.main.pagebeforechange');
    }
  });


  $(function () {
    try {
      var user = new r.User();
      user.set({id: 'current'});
      user.setUrl();
      r.session.getModel(r.session.keys.currentUser, user);
    } catch (e) {
      r.log.error(e, 'RSKYBOX.main.onpageload');
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
      { '#iFeedbackList':           { handler: 'iFeedbackListInit',   events: 'i'   } },
      { '#iFeedbackList':           { handler: 'iFeedbackListShow',   events: 's'   } },
      { '#iFeedback[?]id=.*':       { handler: 'iFeedbackInit',       events: 'i'   } },
      { '#iFeedback[?]id=.*':       { handler: 'iFeedbackShow',       events: 's'   } },
      { '#feedbackList':            { handler: 'feedbackListInit',    events: 'i'   } },
      { '#feedbackList':            { handler: 'feedbackListShow',    events: 's'   } },
      { '#feedback[?]id=.*':        { handler: 'feedbackInit',        events: 'i'   } },
      { '#feedback[?]id=.*':        { handler: 'feedbackShow',        events: 's'   } },
      { '#iLogs':                   { handler: 'iLogsInit',           events: 'i'   } },
      { '#iLogs':                   { handler: 'iLogsShow',           events: 's'   } },
      { '#iLog[?]id=.*':            { handler: 'iLogInit',            events: 'i'   } },
      { '#iLog[?]id=.*':            { handler: 'iLogShow',            events: 's'   } },
      { '#iCrashes':                { handler: 'iCrashesInit',        events: 'i'   } },
      { '#iCrashes':                { handler: 'iCrashesShow',        events: 's'   } },
      { '#iCrash[?]id=.*':          { handler: 'iCrashInit',          events: 'i'   } },
      { '#iCrash[?]id=.*':          { handler: 'iCrashShow',          events: 's'   } },
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
