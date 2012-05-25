var RSKYBOX = (function (r, $) {
  'use strict';

  var
    key = '4a125c4379170658122405',
    url = 'http://free.worldweatheronline.com/feed/weather.ashx',

    error = function (response) {
      try {
        r.log.info('entering', 'error');
      } catch (e) {
        r.log.error(e, 'success');
      }
    },

    success = function (response) {
      console.log(response);
      try {
        var wx = response.data;
        r.log.info('entering', 'success');

        r.log.debug(wx.request[0].query, 'success');
        $('#display').html(wx.request[0].query + ': ' + wx.weather[0].weatherDesc[0].value);
      } catch (e) {
        r.log.error(e, 'success');
      }
    },

    submit = function () {
      try {
        r.log.info('triggered', 'submit');
        $.ajax({
          url: url,
          dataType: 'json',
          data: {
            key: key,
            q: '46528',
            format: 'json'
          },
          success: success,
          error: error,
        });
        return false;
      } catch (e) {
        r.log.error(e, 'submit');
      }
    };

  $(function () {
    try {
      $('#weather').on('submit', submit);
    } catch (e) {
      r.log.error(e, 'jQuery.documentReady');
    }
  });

  return r;

}(RSKYBOX || {}, jQuery));
