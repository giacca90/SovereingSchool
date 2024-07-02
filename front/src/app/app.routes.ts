import { Routes } from '@angular/router';
import { CursosUsuarioComponent } from './components/cursos-usuario/cursos-usuario.component';
import { HomeComponent } from './components/home/home.component';
import { ReproductionComponent } from './components/reproduction/reproduction.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'cursosUsuario', component: CursosUsuarioComponent },
	{ path: 'repro/:id_usuario/:id_curso/:id_clase', component: ReproductionComponent },
	{ path: '**', redirectTo: '' },
];
