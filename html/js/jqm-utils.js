// Dynamically inject pages
//
// Allows for bookmarking, page refreshing, and other proper URL handling for
// URLs that pass information via parameters.
//
// (JQM doc page located at <jqm site>/<version>/docs/pages/page-dynamic.html.)
//
// pairs: an array of hashes of page/function pairs to watch and responded to
//    page: the name of the page without any decoration (such as, '#')
//    function: function called to build the page
function dynamicPages(pairs) {
  $(document).bind('pagebeforechange', function(event, data) {
    // Only handle pagebeforechange calls when loading a page via a URL.
    if (typeof data.toPage === "string") {
      var url = $.mobile.path.parseUrl(data.toPage);

      for (i = 0; i < pairs.length; i++) {
        var pair = pairs[i];
        var re = new RegExp('^' + pair['page']);
        if (re.test(url.hash)) {
          // Get the page hash portion of the URL, make a jQuery element out of it.
          var page = $(pair['page']);

          // calls the function that was passed in via the array of hashes
          pair['function'](page, url);

          data.options.dataUrl = url.href;
          $.mobile.changePage(page, data.options);
          event.preventDefault();
        }
      }
    }
  });
}

// Populate an element with JSON data.
//
// Makes a Ajax REST call to the given URL, then calls the given function to set
// up the element.
//
// restUrl: the URL for the REST call to get the JSON data
// element: the element to be populated
// success: the function to call to actually build the page on success
function jsonPopulate(restUrl, element, success) {
  $.mobile.showPageLoadingMsg();
  $.getJSON(restUrl, function(data) {
    success(element, data);
    $.mobile.hidePageLoadingMsg();
  });
}


// Do an Ajax call using the given method.
//
// restUrl: URL for the REST call
// data: the data to be sent with the request (null for DELETE)
// callback: a function to call on success
function genericJson(method, restUrl, data, success) {
  $.ajax({
    url: restUrl,
    type: method,
    contentType : 'application/json',
    data: data,
    success: success,
    dataType: 'json'
  });
}

// Do an Ajax POST call to create some data on the server.
function postJson(restUrl, data, success) {
  genericJson('POST', restUrl, data, success);
}

// Do an Ajax PUT call to update some data on the server.
function putJson(restUrl, data, success) {
  genericJson('PUT', restUrl, data, success);
}

// Do an Ajax DELETE call to delete some data on the server.
function deleteJson(restUrl, data, success) {
  genericJson('DELETE', restUrl, data, success);
}


// Set/Get the header area of a page.
//
// page: the page we're working with
// markup (optional): if not specified, just return the element
function pageHeader(page, markup) {
  var header = page.children(':jqmData(role=header)');

  if (markup) {
    header.html(markup);
  }
  return header;
}


// Set/Get the content area of a page.
//
// page: the page we're working with
// markup (optional): if not specified, just return the element
function pageContent(page, markup) {
  var content = page.children(':jqmData(role=content)');

  if (markup) {
    content.html(markup);
  }
  return content;
}


// Returns the value of a named parameter from a given JQM URL.
function getParameterByName(url, name) {
  var match = RegExp('[?&]' + name + '=([^&]*)').exec(url.hash);
  return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
}

