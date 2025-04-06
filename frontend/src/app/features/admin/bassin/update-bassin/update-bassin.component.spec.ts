import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateBassinComponent } from './update-bassin.component';

describe('UpdateBassinComponent', () => {
  let component: UpdateBassinComponent;
  let fixture: ComponentFixture<UpdateBassinComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UpdateBassinComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateBassinComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
