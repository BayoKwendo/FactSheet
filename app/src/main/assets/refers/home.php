<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <link href="css/.css" rel="stylesheet" type="text/css" />
     <link href="css/" rel="stylesheet">
    <link href="css/bootstrap.min.css" rel="stylesheet" type="text/css" />
     <link href="font/css/font-awesome.css" rel="stylesheet" />
    <link href='https://fonts.googleapis.com/css?family=Roboto+Condensed:400,700,300' rel='stylesheet' type='text/css'>
     <!--  -->
        <link rel="stylesheet" href="#"/>
         <link rel="stylesheet" href="styles/layout.css"/>
          <script type="text/javascript" src="scripts/jquery-1.4.1.min.js"></script>
          <script type="text/javascript" src="scripts/jquery.cycle.min.js"></script>
          <script type="text/javascript" src="scripts/jquery.cycle.setup.js"></script>
    <title>RAFIKI</title>
  </head>
  <style type="text/css">
        ul{
           padding: 3px;
           background:#ffffff;
          }
    ul li:hover{
          background: yellowgreen;
          }
     ul li{
           display: inline-block;
           position: relative;
           line-height: 25px;
           text-align: left;


          }
   ul li a{
          display: block;
          padding: 8px 25px;
          text-decoration: bold;

          }
 </style>

  <body style="background: #ffe4b5">
 
     <header>
     
        </section>
   <style type="text/css"> {
 box-sizing : border-box }
/* Slideshow container */
.slideshow-container {
max-width : 100% ;
margin : auto ;
}
.mySlides {
display: none ;
}
/* Next & previous buttons */
.prev, .next {
cursor : pointer;
position : absolute ;
top : 50% ;
width: auto ;
margin-top : -22px ;
padding: 16px ;
color: white ;
font-weight : bold ;
font-size : 18px ;
transition : 0.6s ease ;
border-radius : 0 3px 3px 0 ;
}
/* Position the "next button" to the right */
.next {
right: 0 ;
border-radius : 3px 0 0 3px ;
}
/* On hover, add a black background color with a little bit see-through */
.prev:hover, .next:hover {
background-color : rgba(0,0,0,0.8) ;
}
/* Caption text */
.text {
color: #f2f2f2 ;
font-size : 15px ;
padding: 8px 12px ;
position : absolute ;
bottom : 8px ;
width: 100% ;
text-align : center ;
}
/* Number text (1/3 etc) */
.numbertext {
color: #f2f2f2 ;
font-size : 12px ;
padding: 8px 12px ;
position : absolute ;
top : 0;
}
/* The dots/bullets/indicators */
.dot {
cursor : pointer;
height : 13px ;
width: 13px ;
margin : 0 2px ;
background-color : #bbb ;
border-radius : 50% ;
display: inline-block ;
transition : background-color 0.6s ease ;
}
.active, .dot:hover {
background-color : #717171 ;
}
/* Fading animation */
.fade {
-webkit-animation-name : fade ;
-webkit-animation-duration : 1.5s ;
animation-name : fade ;
animation-duration : 1.5s ;
}
@-webkit-keyframes fade {
from {opacity: .4 }
to { opacity: 1}
}
@keyframes fade {
from {opacity: .4 }
to { opacity: 1}
}
</style>
<script type="text/javascript">

var slideIndex = 0 ;
showSlides();
function showSlides() {
var i;
var slides = document.getElementsByClassName( "mySlides" );
for (i = 0 ; i < slides.length; i++) {
slides[i].style.display = "none" ;
}
slideIndex++;
if (slideIndex> slides.length) {slideIndex =
1 }
slides[slideIndex- 1 ].style.display = "block" ;
setTimeout(showSlides,
5000 ); // Change image every 2 seconds
}
 function blinker(){
    $('.blinking').fadeOut(500);
    $('.blinking').fadeIn(500);
  }
  setInterval(blinker,3000);


</script>
    </header>
    <footer>
     <div class="col-md-12" >
      <div class='text-center' >
        <h4 class='text-center'><b>Follow Us On</b></h4>
      <ul class='text-center' style="list-style:none; display:inline-flex; font-size:20px;" style="background:#b4b4b4;" >
        <li ><a title="twitter" class="fa fa-twitter" style="background:#b4b4b4; color: lightblue;" 
        href="http://www.twitter.com/HIVFactSheet " target="_blank"></a></li>
        <li ><a title="facebook" class="fa fa-facebook" style="background:#b4b4b4; " 
        href="http://www.facebook.com/HIVFactSheet/" target="_blank"></a></li>
      </ul>
      <br>
     </div>
     </div>
  
      </footer>

</div>
  </body>
  <script type="text/javascript" src="js/jquery2.js"></script>
      <script type="text/javascript" src="js/bootstrap.min.js"></script>
      <script type="text/javascript" src="js/jquery.validate.min.js"></script>
      <script type="text/javascript" src="js/register.js"></script>
</html>