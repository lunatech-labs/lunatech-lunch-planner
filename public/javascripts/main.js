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

