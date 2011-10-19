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


// Do an Ajax POST call to update some data on the server.
//
// restUrl: URL for the REST call
// data: the data to be sent with the request
// callback: a function to call on success
function postJson(restUrl, data, success) {
  $.ajax({
    url: restUrl,
    type: 'POST',
    contentType : 'application/json',
    data: data,
    success: success,
    dataType: 'json'
  });
}


// Do an Ajax PUT call to update some data on the server.
//
// restUrl: URL for the REST call
// data: the data to be sent with the request
// callback: a function to call on success
function putJson(restUrl, data, success) {
  $.ajax({
    url: restUrl,
    type: 'PUT',
    contentType : 'application/json',
    data: data,
    success: success,
    dataType: 'json'
  });
}


// Set/Get the header area of a page.
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


// Set/Get the content area of a page.
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


// Returns the value of a named parameter from a given JQM URL.
function getParameterByName(url, name) {
  var match = RegExp('[?&]' + name + '=([^&]*)').exec(url.hash);
  return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
}

