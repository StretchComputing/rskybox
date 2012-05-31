var RSKYBOX = (function (r, $) {
  'use strict';


  var
    key = '4a125c4379170658122405',
    num_of_results = 3,
    url = 'http://free.worldweatheronline.com/feed/weather.ashx',


    flash = function (message) {
      $('#flash').html(message).fadeIn().delay(3000).fadeOut();
    },


    showMessage = function (message) {
      try {
        flash(message);
        r.log.warn(message, 'showMessage');
      } catch (e) {
        r.log.error(e, 'showMessage');
      }
    },


    display = function (wx) {
      try {
        r.log.debug(JSON.stringify(wx), 'success');
        $('.query').text(wx.request[0].query);
        $('.current .time').text(wx.current_condition[0].observation_time);
        $('.current .desc').text(wx.current_condition[0].weatherDesc[0].value);
        $('.current .icon-url').attr('src', wx.current_condition[0].weatherIconUrl[0].value);
        $('.current .winddir').text(wx.current_condition[0].winddir16Point);
        $('.current .windspeed').text(wx.current_condition[0].windspeedMiles);
        $('.forecast .date').text(wx.weather[0].date);
        $('.forecast .desc').text(wx.weather[0].weatherDesc[0].value);
        $('.forecast .icon-url').attr('src', wx.weather[0].weatherIconUrl[0].value);
        $('.forecast .winddir').text(wx.weather[0].winddir16Point);
        $('.forecast .windspeed').text(wx.weather[0].windspeedMiles);
        $('#display').show();
      } catch (e) {
        r.log.error(e, 'display');
      }
    },


    success = function (response) {
      try {
        var message, wx = response.data;
        r.log.info('entering', 'success');

        if (wx.error) {
          showMessage(wx.error[0].msg);
          return;
        }
        r.wx = wx;
        display(wx);
      } catch (e) {
        r.log.error(e, 'success');
      }
    },


    error = function (response) {
      try {
        r.log.info('entering', 'error');
        showMessage(response);
      } catch (e) {
        r.log.error(e, 'error');
      }
    },


    submit = function () {
      try {
        r.log.info('triggered', 'submit');
        $.ajax({
          url: url,
          dataType: 'jsonp',
          data: {
            key: key,
            q: $('#location').val(),
            format: 'json',
            num_of_days: num_of_results,
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
