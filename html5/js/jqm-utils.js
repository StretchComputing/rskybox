// Module Pattern
var RMODULE = (function (my, $) {
  'use strict';

  var
    genericJson,
    hidePageLoadingMessage,
    pageLoad,
    pageLoadCount,
    showPageLoadingMessage;

  my.getRestPrefix = function () {
    return '/rest/v1';
  };

  // Dynamically inject pages
  //
  // Allows for bookmarking, page refreshing, and other proper URL handling for
  // URLs that pass information via parameters.
  //
  // (JQM doc page located at <jqm site>/<version>/docs/pages/page-dynamic.html.)
  //
  // pairs: an array of hashes of page/function pairs to watch and responded to
  // page: the name of the page without any decoration (such as, '#')
  // function: function called to build the page
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
  // restUrl: URL for the REST call
  // data: the data to be sent with the request (null for DELETE)
  // callback: a function to call on success
  genericJson = function (method, restUrl, data, success) {
    $.ajax({
      url : restUrl,
      type : method,
      contentType : 'application/json',
      data : data,
      success : success,
      dataType : 'json'
    });
  };

  // Wrappers for doing various JSON methods
  my.postJson = function (restUrl, data, success) {
    genericJson('POST', restUrl, data, success);
  };
  my.putJson = function (restUrl, data, success) {
    genericJson('PUT', restUrl, data, success);
  };
  my.deleteJson = function (restUrl, data, success) {
    genericJson('DELETE', restUrl, data, success);
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

  // Returns the value of a named parameter from a given JQM URL.
  my.getParameterByName = function (url, name) {
    var match = new RegExp('[?&]' + name + '=([^&]*)').exec(url.hash);
    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
  };

  return my;
}(RMODULE || {}, jQuery));
