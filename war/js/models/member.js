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
    },

    validate: function (attrs) {
      if (r.isValidEmailAddress(attrs.emailAddress) && attrs.role) {
        r.log.debug('Member: member is valid.');
        return;
      }
      return 'A valid email address and selected role are required.';
    }
  });


  r.Members = r.BaseCollection.extend({
    model: r.Member,
    apiUrl: '/appMembers',

    parse: function (response) {
      return response.appMembers;
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
