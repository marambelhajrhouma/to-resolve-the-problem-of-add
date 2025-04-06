import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/authentication/auth.service';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css']
})
export class HomePageComponent implements OnInit {

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    if (typeof window !== "undefined") {
      console.log(window.innerWidth); 
    }

    
    window.addEventListener("load", () => {
      const preloader = document.getElementById("preloder");
      if (preloader) {
        preloader.style.display = "none";
      }
    });
  }


}