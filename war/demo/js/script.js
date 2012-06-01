var RSKYBOX = (function (r, $) {
  'use strict';


  var
    key = '4a125c4379170658122405',
    num_of_results = 3,
    url = 'http://free.worldweatheronline.com/feed/weather.ashx',


    flash = function (message) {
      try {
        $('#flash').html(message).fadeIn().delay(3000).fadeOut();
      } catch (e) {
        r.log.error(e, 'flash');
      }
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
        var cc, clone, current = $('.current'), forecast = $('.forecast'), template = $('.template');

        r.log.debug(JSON.stringify(wx), 'success');
        $('.query').text(wx.request[0].query);

        cc = wx.current_condition[0];
        current.find('.desc').text(cc.weatherDesc[0].value);
        current.find('.temp').text(cc.temp_F);
        current.find('.humidity').text(cc.humidity);
        current.find('.icon-url').attr('src', cc.weatherIconUrl[0].value);
        current.find('.winddir').text(cc.winddir16Point);
        current.find('.windspeed').text(cc.windspeedMiles);

        forecast.html('');
        if (wx.weather.length) {
          wx.weather.forEach(function (w) {
            clone = template.clone();

            clone.find('.date').text(w.date);
            clone.find('.desc').text(w.weatherDesc[0].value);
            clone.find('.temp-min').text(w.tempMinF);
            clone.find('.temp-max').text(w.tempMaxF);
            clone.find('.icon-url').attr('src', w.weatherIconUrl[0].value);
            clone.find('.winddir').text(w.winddir16Point);
            clone.find('.windspeed').text(w.windspeedMiles);
            forecast.append(clone);
            clone.removeClass('template').addClass('day');
          });
        }

        $('.display').show();
        $('footer').hide();
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


    reset = function () {
      try {
        r.log.info('triggered', 'reset');
        $('#location').val('');
        $('.display').hide();
        $('footer').show();
      } catch (e) {
        r.log.error(e, 'reset');
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
            num_of_days: $('#days').val(),
          },
          success: success,
          error: error,
        });
        return false;
      } catch (e) {
        r.log.error(e, 'submit');
      }
    },

    generateError = function () {
      try {
        r.log.error(new Error('a generated error'), 'generateError');
      } catch (e) {
        r.log.error(e, 'generateError');
      }
    };


  $(function () {
    try {
      $('#weather').on('submit', submit);
      $('.reset').on('click', reset);
      $('.error').on('click', generateError);
    } catch (e) {
      r.log.error(e, 'jQuery.documentReady');
    }
  });


  return r;

}(RSKYBOX || {}, jQuery));
