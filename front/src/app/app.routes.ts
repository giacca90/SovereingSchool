import { Routes } from '@angular/router';
import { CursosComponent } from './components/cursos/cursos.component';
import { HomeComponent } from './components/home/home.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'cursos', component: CursosComponent },
	{ path: '**', redirectTo: '' },
];
