var RSKYBOX = (function (r, $) {
  'use strict';


  r.Member = r.BaseModel.extend({
    apiUrl: '/appMembers',
    fields: {
      id: null,
      appId: null,
      emailAddress: null,
      phoneNumber: null,
      date: null,
      role: null,
      status: null,
      confirmationCode: null,  // used during member confirmation only
    },

    validate: function (attrs) {
      try {
        if (r.isValidEmailAddress(attrs.emailAddress) && (attrs.role || attrs.memberConfirmation)) {
          r.log.info('member is valid', 'Member.validate');
          return;
        }
        return 'A valid email address and selected role are required.';
      } catch (e) {
        r.log.error(e, 'Members.validate');
      }
    }
  });


  r.Members = r.BaseCollection.extend({
    model: r.Member,
    apiUrl: '/appMembers',

    parse: function (response) {
      try {
        return response.appMembers;
      } catch (e) {
        r.log.error(e, 'Members.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
