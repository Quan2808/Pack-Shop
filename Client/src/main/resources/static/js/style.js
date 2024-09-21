let lastScrollTop = 0;
const navbar = document.querySelector('.navbar');

window.addEventListener('scroll', function () {
  let scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  
  if (scrollTop > lastScrollTop) {
    // Trượt xuống - ẩn header
    navbar.style.top = '-100px';
  } else {
    // Trượt lên - hiển thị header
    navbar.style.top = '0';
  }
  
  lastScrollTop = scrollTop;
});
