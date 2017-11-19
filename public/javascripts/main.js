$(function () {
    $('select#set-all').change(function () {
        var value = $(this).val();
        if (value === "Not Attending") {
            $('.select-not-attending').prop("selected", true);
        }

        if (value.endsWith("Rotterdam") || value.endsWith("Amsterdam")) {
            var location = value.split(":")[1];
            var option = $('.location-' + location);
            option.prop("selected", true);
        }
    });

});

$(function () {
    $('.input-daterange').datepicker({
        format: 'dd-mm-yyyy',
        todayBtn: true,
        todayHighlight: true
    });
});

$(function () {
    $('#monthSelection').change(function() {
       $('#monthNumber').submit();
    });
});

$(function (defaultDateStart, defaultDateEnd) {
    $(".daterange").change(function () {
        var dateStart = document.getElementById("dateStart").value;
        var dateEnd = document.getElementById("dateEnd").value;

        if (dateStart !== defaultDateStart || dateEnd !== defaultDateEnd) {
            $('#filterDateRange').submit();
        }
    });
});

$(function () {
    $('#formAttendance').submit(function () {
        $('.select-not-attending:selected').each(function () {
            $(this).parents('td').append('<input type="hidden" name="menuPerDayUuidsNotAttending[]" value="' + $(this).attr("value") + '" />');
            $(this).prop("selected", false);
        });

        $('select option:selected').filter(function () {
            return !this.value || $.trim(this.value).length == 0;
        }).parent('select').remove();

        $("#set-all").remove();

        return true;
    });
});

$(document).ready(function() {
    $('button.js-delete-confirmation').click(function() {
        return confirm("Are you sure you want to delete?");
    })
});

$(function () {
    var dateObject = new Date();
    var currentDate = {
        date : dateObject.getDate(),
        hour : dateObject.getHours()
    };

    var dateFormat = formatDate(dateObject);
    disableDropdown(dateFormat);

    dateObject.setDate(currentDate.date + 1)
    var nextDateFormat = formatDate(dateObject);
    if (currentDate.hour >= 13) {
        disableDropdown(nextDateFormat);
    }

    function formatDate(dateObj) {
        return dateObj.getDate() + '-' + (dateObj.getMonth() + 1) + '-' + dateObj.getFullYear();
    }

    function disableDropdown(dateFormat) {
        var obj = $('.select-' + dateFormat);
        var value = obj.val();
        var text = $('.select-' + dateFormat + ' > option:selected').text();

        var parents = obj.parents('td');
        parents.append('<p>'+ text + '</p>');

        if (text === "Not Attending") {
            parents.append('<input type="hidden" name="menuPerDayUuidsNotAttending[]" value="' + value + '" />');
        } else if (value !== "") {
            parents.append('<input type="hidden" name="menuPerDayUuids[]" value="' + value + '"/>');
        }
        obj.remove();
    }
});