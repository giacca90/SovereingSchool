import { Routes } from '@angular/router';
import { CursosComponent } from './components/cursos/cursos.component';
import { HomeComponent } from './components/home/home.component';
import { ReproductionComponent } from './components/reproduction/reproduction.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'cursos', component: CursosComponent },
	{ path: 'repro/:id_clase', component: ReproductionComponent },
	{ path: '**', redirectTo: '' },
];
