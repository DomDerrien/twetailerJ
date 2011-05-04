$(document).ready(function(){

	//---------------------------
	// Initialize FancyBox
	//---------------------------	
	$(".lightbox").fancybox({
		'speedIn'		:	600, 
		'speedOut'		:	200
	});
	
	//---------------------------
	// Twitter widget
	//---------------------------
	if( $("#twitter").length ){
		
		// Translate the timeago plugin
		jQuery.timeago.settings.strings = { 
			suffixAgo: "ago",
			suffixFromNow: "from now",
			seconds: "Less than a minute",
			minute: "About a minute",
			minutes: "%d minutes",
			hour: "About an hour",
			hours: "About %d hours",
			day: "A day",
			days: "%d days",
			month: "About a month",
			months: "%d months",
			year: "About a year",
			years: "%d years"
		};
		
		var yourTwitterUsername = "ASEconomy"; //Insert your twitter username
		
		$.ajax({
			url : "http://twitter.com/statuses/user_timeline/"+yourTwitterUsername+".json?callback=?",
			dataType : "json",
			timeout: 15000,
			
			success : function(data){
				var time = data[0].created_at,
					text = data[0].text,
					id = data[0].id_str,
					twitterDiv = $("#twitter").find("div");
					
				time = time.replace(/(\+\S+) (.*)/, '$2');
				time = $.timeago( new Date( Date.parse( time ) ) );
										
				text = text.replace(/((https?|s?ftp|ssh)\:\/\/[^"\s\<\>]*[^.,;'">\:\s\<\>\)\]\!])/g, function(url){
								return '<a href="'+url+'" target="_blank">'+url+'</a>'});
				text = text.replace(/@(\w+)/g, function(url){
								return '<a href="http://www.twitter.com/'+url.substring(1)+'" target="_blank">'+url+'</a>'});
				text = text.replace(/#(\w+)/g, function(url){
								return '<a href="http://twitter.com/#!/search?q=%23'+url.substring(1)+'" target="_blank">'+url+'</a>'});
				text = "<a href='http://twitter.com/"+yourTwitterUsername+"/status/"+id+"' class='status'>" +time+ "</a> " + text;
				twitterDiv.html(text);
				// Adjust width for older browsers
				twitterDiv.css({ display: "inline"});
				twitterDiv.width( twitterDiv.width() );
				twitterDiv.css({ display: "block"});
			},
			
			error : function(){
				$("#twitter").find("div").html("There was an error connecting to your Twitter account");
			}
		});
		
	}
	
	//---------------------------
	// Content Slider
	//---------------------------
	if( $(".slider").length ){
		
		// Init the slider
		var slider = $(".slider"),
			slideWidth = slider.find("li").eq(0).width(),
			num = slider.find("li").length,
			sliderController = $(".sliderController");

		slider.width( num * slideWidth );
		
		// center the arrows and add click event
		$(".arrow").height(
			slider.height()
		).click( function(){
			slideTo( $(this).attr("rel") );
			return false;
		} );
		
		// build the controller
		for( i=0; i<num; i++ ){
			var li = $('<li><a href="#"></a></li>');
			li.click(function(){
				slideTo( $(this).index() );
				return false;
			}).appendTo(sliderController);
		}
		
		// Set width to the controller to center it
		sliderController.width(
			num * 25
		).find("li").eq(0).addClass("current");
		
		// Do slide
		function slideTo( next ){
			var current = sliderController.find(".current"),
				currentIndex = current.index();
				
			if( next == "right" ) { 
				next = currentIndex + 1;
				if( next == num ) { next = 0; }
			}
			if( next == "left" ) {
				next = currentIndex - 1; 
				if( next < 0 ) { next = num-1; }
			}
			
			if( (next < num) && !(next < 0) ) {
			
				slider.animate({
					left: - ( next * slideWidth )
				});
				
				current.removeClass("current");
				sliderController.find("li").eq(next).addClass("current");
				
			}	
		}
	
	}
	
	//---------------------------
	// Autofilling forms using placeholders
	//---------------------------
	if( !supports_placeholder() ){
		// If your browser does not support placeholders
		$("input[type=text], textarea").each(function(){
			$(this).val($(this).attr('placeholder'));
		}).focus(function(){
			if($(this).val() == $(this).attr('placeholder')) { $(this).val(""); }
		}).blur(function(){
			if($(this).val() == "") { $(this).val($(this).attr('placeholder')); }
		});
	}
	
	// Test placeholder support
	function supports_placeholder() {
		var i = document.createElement('input');
		return 'placeholder' in i;
	}

});