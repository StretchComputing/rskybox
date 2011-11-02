var RMODULE = (function (my, $) {
  'use strict';

  my.getMobileCarriersPath = function () {
    return '/mobileCarriers';
  };

  // Simple RegEx to ensure a valid phone number format.
  my.validPhoneNumber = function (phoneNumber) {
    return (/^\(?([0-9]{3})\)?[\-. ]?([0-9]{3})[\-. ]?([0-9]{4})$/).test(phoneNumber);
  };

  my.validEmailAddress = function (emailAddress) {
    return (/^[a-z0-9!#$%&'*+\/=?\^_`{|}~\-]+(?:\.[a-z0-9!#$%&'*+\/=?\^_`{|}~\-]+)*@(?:[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?$/).test(emailAddress);
  };

  // Build a list of mobile carriers options.
  //
  // carriers: array of id/name pairs
  // returns: the generate markup
  my.getNoCarrierIndicator = function () {
    return '-1';
  };
  my.carrierOptions = function (carriers) {
    var i, markup;

    markup = '<option value="' + my.getNoCarrierIndicator() + '">Mobile Carrier</option>';
    for (i = 0; i < carriers.length; i += 1) {
      markup += '<option value="' + carriers[i].id + '">' + carriers[i].name + '</option>';
    }
    return markup;
  };


  my.enableSmsDetails = function (enabled) {
    $('#phoneNumber').prop('disabled', !enabled);
    $('#mobileCarrierId').prop('disabled', !enabled).selectmenu('refresh');
  };

  my.validateSms = function () {
    if (!$('#sendSmsNotifications').prop('checked')) { return true; }

    if (!my.validPhoneNumber($('#phoneNumber').val())) {
      window.alert('Please enter a valid phone number.');
      return false;
    }
    if ($('#mobileCarrierId').val() === my.getNoCarrierIndicator()) {
      window.alert('Please select a mobile carrier.');
      return false;
    }

    return true;
  };

  return my;
}(RMODULE || {}, jQuery));
