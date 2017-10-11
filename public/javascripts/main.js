$(function(){
    'use strict';

    /**
     * Will perform "smart" select all. Will only check one item per day.
     * 1. If there's already a check for that day, it will remain unchanged.
     * 2. If there's only one meal for that day, it will be checked regardless of location.
     * 3. It will always go for the default location, which is Rotterdam. Unless we implement a "preferred location" per user.
     * 4. If there's more than one meal with the same location, it will check the first meal in the list.
     */
    function smartSelectAll(defaultLocation, className) {
        var dates = $("input[data-date]" + className).map(function() {
            return $(this).attr("data-date");
        }).get();
        var uniqueDates = jQuery.unique(dates);

        uniqueDates.forEach(function(date) {
            var hasCheck = false;
            var inputsByDate = $("input[data-date='" + date + "']" + className);

            var checkedInputsByDate = $("input[data-date='" + date + "']" + className +":checked");
            if(checkedInputsByDate.length === 0) {
                inputsByDate.each(function() {
                    if(inputsByDate.length === 1) {
                        this.checked = true;
                    } else {
                        var location = $(this).parents(".schedule-row").children(".js-menu-location").attr("value");
                        if (!hasCheck && defaultLocation === location) {
                            this.checked = true;
                            hasCheck = true;
                        }
                    }
                })
            }
        });
    }

    $('.select-all').click(function() {
        if (this.checked) {
            handleSmartSelectAll(this);
        } else {
            handleUncheckAll(this);
        }
    });

    function handleSmartSelectAll(obj) {
        if($(obj).hasClass("js-select-attending")) {
            smartSelectAll("Rotterdam", ".schedule.attending");
            $("input.schedule.not-attending").prop("checked", false);
            $("input.js-select-not-attending").prop("checked", false);
        } else {
            smartSelectAll("Rotterdam", ".schedule.not-attending");
            $("input.schedule.attending").prop("checked", false);
            $("input.js-select-attending").prop("checked", false);
        }
    }

    function handleUncheckAll(obj) {
        if($(obj).hasClass("js-select-attending")) {
            $("input.schedule.attending").prop("checked", false);
        } else {
            $("input.schedule.not-attending").prop("checked", false);
        }
    }
});

$(function allSelected(){
    var attendingAllSelected = $('.schedule.attending:checked').length === $('.schedule.attending').length;
    if(attendingAllSelected) {
        $('input.js-select-attending').prop("checked", true);
    }
    var notAttendingAllSelected = $('.schedule.not-attending:checked').length === $('.schedule.not-attending').length;
    if(notAttendingAllSelected) {
        $('input.js-select-not-attending').prop("checked", true);
    }
});

$(function(){
    $('.input-daterange').datepicker({
        format: 'dd-mm-yyyy',
        todayBtn: true,
        todayHighlight: true
    });
});

$(function(defaultDateStart, defaultDateEnd){
    $(".daterange").change(function(){
        var dateStart = document.getElementById("dateStart").value;
        var dateEnd = document.getElementById("dateEnd").value;

        if(dateStart !== defaultDateStart || dateEnd !== defaultDateEnd) {
            $('#filterDateRange').submit();
        }
    });
});

$(function verifySelectedMeals() {
    $(".schedule").change(function () {
        if (this.checked) {
            var date = $(this).attr("data-date");
            var index = $(this).attr("data-index");
            handleTwoCheckboxes(this, index)
            var checkboxes = $("input[data-date='" + date + "']:checked");
            if (checkboxes.length >= 2) {
                this.checked = false;
                $.toaster({
                    priority: 'danger',
                    title: 'Ah ah ah!',
                    message: 'You can only make one decision per day. Date:' + date
                });
            }
        }
    });

    function handleTwoCheckboxes(obj, index) {
        if ($(obj).hasClass("attending")) {
            $("input[data-index='" + index + "'].not-attending").prop("checked", false);
        } else {
            $("input[data-index='" + index + "'].attending").prop("checked", false);
        }
    }
});

$(function() {
    $('#formAttendance').submit(function () {
        $('.schedule:checked').each(function () {
            $(this).append('<input type="hidden" name="menuDate[]" value="' + $(this).parents(".schedule-row").children(".js-menu-date").attr("value") + '" />');
        });
        return true;
    });
});

