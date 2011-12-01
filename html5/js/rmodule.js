'use strict';

// Module Pattern
var RMODULE = (function (my, $) {

  var
    genericJson,
    hidePageLoadingMessage,
    pageLoad,
    pageLoadCount,
    showPageLoadingMessage;


  my.ROLES = {
    owner : {
      id : 'owner',
      name : 'Owner'
    },
    manager : {
      id : 'manager',
      name : 'Manager'
    },
    member : {
      id : 'member',
      name : 'Member'
    }
  };


  // Returns the currently valid REST path prefix.
  my.getRestPrefix = function () {
    var restUrl;

    restUrl = '\/rest\/v1'
    if (window.location.search) {
      restUrl += '\/applications\/' + my.getParameterByName(window.location.search, 'id');
    }
    return restUrl;
  };


  // Dynamically inject pages
  //
  // Allows for bookmarking, page refreshing, and other proper URL handling for
  // URLs that pass information via parameters.
  //
  // (JQM doc page located at <jqm site>/<version>/docs/pages/page-dynamic.html.)
  //
  // pairs: an array of hashes of page/function pairs to watch and responded to
  // pairs[].page: the name of the page without any decoration (such as, '#')
  // pairs[].function: function called to build the page
  my.dynamicPages = function (pairs) {
    $(document).bind('pagebeforechange', function (event, data) {
      var i, page, pair, re, url;

      // Only handle pagebeforechange calls when loading a page via a URL.
      if (typeof data.toPage === "string") {
        url = $.mobile.path.parseUrl(data.toPage);

        for (i = 0; i < pairs.length; i += 1) {
          pair = pairs[i];
          re = new RegExp('^' + pair.page);
          if (re.test(url.hash)) {
            // Get the page hash portion of the URL, make a jQuery element out of it.
            page = $(pair.page);

            // calls the function that was passed in via the array of hashes
            pair['function'](page, url);

            data.options.dataUrl = url.href;
            $.mobile.changePage(page, data.options);
            event.preventDefault();
          }
        }
      }
    });
  };


  // Get the current user and call the success function when done.
  my.getCurrentUser = function (success, noUser) {
    var extra;

    extra = {};
    extra.statusCode = {
      401: function () {
        hidePageLoadingMessage();
        noUser && noUser();
      }
    }
    showPageLoadingMessage();
    genericJson('GET', my.getRestPrefix() + '\/users\/current', null, function (user) {
      success(user);
      hidePageLoadingMessage();
    }, extra);
  };


  // Browser redirect to another URL.
  my.redirect = function (url) {
    window.location = url;
  };


  // Track how many page loading messages are on the stack.
  pageLoadCount = 0;
  pageLoad = function (operator) {
    switch (operator) {
    case 'decrement':
      pageLoadCount -= pageLoadCount === 0 ? 0 : 1;
      break;
    case 'increment':
      pageLoadCount += 1;
      break;
    default:
      window.console.log('pageLoadingCount called with inappropriate operator.');
    }
    return pageLoadCount;
  };


  // Manage showing/hiding the page loading message based on the number of times it's been called.
  hidePageLoadingMessage = function () {
    if (pageLoad('decrement') <= 0) {
      $.mobile.hidePageLoadingMsg();
    }
  };

  showPageLoadingMessage = function () {
    pageLoad('increment');
    $.mobile.showPageLoadingMsg();
  };


  // Populate an element with JSON data.
  //
  // Makes a Ajax REST call to the given URL, then calls the given function to set
  // up the element.
  //
  // restUrl: the URL for the REST call to get the JSON data
  // element: the element to be populated
  // success: the function to call to actually build the page on success
  my.jsonPopulate = function (restUrl, element, success) {
    showPageLoadingMessage();
    $.getJSON(restUrl, function (data) {
      success(element, data);
      hidePageLoadingMessage();
    });
  };


  // Do an Ajax call using the given method.
  //
  // method: HTTP method.  One of: GET, PUT, POST, DELETE
  // restUrl: URL for the REST call
  // data: the data to be sent with the request (null for DELETE)
  // success: a function to call on success
  // extra: extra Ajax properties passed in an object
  genericJson = function (method, restUrl, data, success, extra) {
    var ajax, x;

    ajax = {};
    ajax.url = restUrl;
    ajax.type = method;
    ajax.contentType = "application\/json";
    ajax.data = data;
    ajax.success = success;

    for (x in extra) {
      ajax[x] = extra[x];
    }

    $.ajax(ajax);
  };


  // Wrappers for doing various JSON methods
  //
  // For 'GET' use $.getJSON() is provided directly by jQuery.
  //
  my.postJson = function (restUrl, data, success, extra) {
    genericJson('POST', restUrl, data, success, extra);
  };

  my.putJson = function (restUrl, data, success, extra) {
    genericJson('PUT', restUrl, data, success, extra);
  };

  my.deleteJson = function (restUrl, data, success, extra) {
    genericJson('DELETE', restUrl, data, success, extra);
  };


  // Set/Get the header area of a page.
  //
  // page: the page we're working with
  // markup (optional): if not specified, just return the element
  my.pageHeader = function (page, markup) {
    var header = page.children(':jqmData(role=header)');

    if (markup) {
      header.html(markup);
    }
    return header;
  };


  // Set/Get the content area of a page.
  //
  // page: the page we're working with
  // markup (optional): if not specified, just return the element
  my.pageContent = function (page, markup) {
    var content = page.children(':jqmData(role=content)');

    if (markup) {
      content.html(markup);
    }
    return content;
  };


  // Build and return a query string from an object.
  //
  // props: the object containing the name/value pairs for the query string
  my.getQueryString = function (props) {
    var prop, query;

    query = "?";
    for (prop in props) {
      query += prop + '=' + props[prop] + '&';
    }
    query = query.slice(0, query.length - 1);

    return query;
  };


  // Returns the value of a named parameter from a given JQM URL.
  my.getParameterByName = function (url, name) {
    var match = new RegExp('[?&]' + name + '=([^&]*)').exec(url);
    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
  };


  // Builds the URL needed for getting the mobile carriers.
  my.getMobileCarriersUrl = function () {
    return my.getRestPrefix() + '/mobileCarriers';
  };


  // Simple RegEx to ensure a valid phone number format.
  my.isValidPhoneNumber = function (phoneNumber) {
    return (/^\(?([0-9]{3})\)?[\-. ]?([0-9]{3})[\-. ]?([0-9]{4})$/).test(phoneNumber);
  };


  // Simple RegEx to ensure a valid email address format.
  my.isValidEmailAddress = function (emailAddress) {
    return (/^[a-z0-9!#$%&'*+\/=?\^_`{|}~\-]+(?:\.[a-z0-9!#$%&'*+\/=?\^_`{|}~\-]+)*@(?:[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?$/).test(emailAddress);
  };


  // Basic determination of something was selected for carrierId.
  my.isValidCarrier = function (carrierId) {
    // null and '' are both considered false
    if (!carrierId) { return false; }

    return carrierId !== my.getNoOptionSelected();
  }


  // Build an option list for a select control.
  //
  // options: array of id/name pairs
  // returns: the generate markup
  my.getNoOptionSelected = function () {
    return '-1';
  };
  my.getOptionsForSelect = function (options, title) {
    var i, markup;

    markup = '<option value="' + my.getNoOptionSelected() + '">' + title + '</option>';
    for (i = 0; i < options.length; i += 1) {
      markup += '<option value="' + options[i].id + '">' + options[i].name + '</option>';
    }
    return markup;
  };


  // Toggle SMS controls.
  //
  // TODO rename this 'toggleSmsDetails'
  my.enableSmsDetails = function (enabled) {
    $('#phoneNumber').prop('disabled', !enabled);
    $('#mobileCarrierId').prop('disabled', !enabled).selectmenu('refresh');
  };


  // Performs business rules validation on SMS details.
  //
  // TODO decouple UI elements from validation
  // TODO rename this 'isValidSmsSettings' (maybe)
  my.validateSms = function () {
    if (!$('#sendSmsNotifications').prop('checked')) {
      return true;
    }

    if (!my.isValidPhoneNumber($('#phoneNumber').val())) {
      window.alert('Please enter a valid phone number.');
      return false;
    }
    if ($('#mobileCarrierId').val() === my.getNoOptionSelected()) {
      window.alert('Please select a mobile carrier.');
      return false;
    }

    return true;
  };


  return my;
}(RMODULE || {}, jQuery));
