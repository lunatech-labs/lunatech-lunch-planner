$(function(){
    'use strict';

    /**
     * Will perform "smart" select all. Will only check one item per day.
     * 1. If there's already a check for that day, it will remain unchanged.
     * 2. If there's only one meal for that day, it will be checked regardless of location.
     * 3. It will always go for the default location, which is Rotterdam. Unless we implement a "preferred location" per user.
     * 4. If there's more than one meal with the same location, it will check the first meal in the list.
     */
    function smartSelectAll(defaultLocation) {
        var dates = $("input[data-date]").map(function() {
            return $(this).attr("data-date");
        }).get();
        var uniqueDates = jQuery.unique(dates);

        uniqueDates.forEach(function(date) {
            var hasCheck = false;
            var inputsByDate = $("input[data-date='" + date + "']");

            var checkedInputsByDate = $("input[data-date='" + date + "']:checked");
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

    $('#select-all').click(function() {
        if (this.checked) {
            smartSelectAll("Rotterdam");
        } else {
            $(':checkbox').each(function () {
                this.checked = false;
            });
        }
    });
});

$(function allSelected(){
    var allSelected = $('.schedule:checked').length === $('.schedule').length;

    if(allSelected) {
        $('input#select-all').prop("checked", true);
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
    $('.daterange').change(function(){
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
            var checkboxes = $("input[data-date='" + date + "']:checked");
            if (checkboxes.length >= 2) {
                this.checked = false;
                $.toaster({
                    priority: 'danger',
                    title: 'Ah ah ah!',
                    message: 'You can only attend one meal per day.'
                });
            }
        }
    });

$(document).ready(function() {
    allSelected();

    $('button.js-delete-confirmation').click(function() {
        return confirm("Are you sure you want to delete?");
    })
});

$(function() {
    $('#formAttendance').submit(function (eventObj) {
        $('.schedule:checked').each(function () {
            $(this).append('<input type="hidden" name="menuDate[]" value="' + $(this).parents(".schedule-row").children(".js-menu-date").attr("value") + '" />');
        });
        return true;
    });
});
