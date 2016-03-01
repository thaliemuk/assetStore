$(document).ready(function () {
	$.get('html\\gallery.txt', function(data) {
       /* //split on new lines
        var lines = data.split('\n');
        //create select
        var dropdown = $('<select>');
        //iterate over lines of file and create a option element
        for(var i=0;i<lines.length;i++) {
            //create option
            var el = $('<option value="'+i+'">'+lines[i]+'</option>');
            //append option to select
            $(dropdown).append(el);
        }
        //append select to page
        $('body').append(dropdown);
        var galleryData ={};
	var galleryHtml = Mustache.render()
	$("#footer").html();
*/});

	$(".serachIcon").on('click',function(){
		var textToSearch = $(".searchBox-text").text();
	});
});

	