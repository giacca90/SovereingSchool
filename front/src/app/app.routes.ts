import { Routes } from '@angular/router';
import { CursoComponent } from './components/curso/curso.component';
import { CursosUsuarioComponent } from './components/cursos-usuario/cursos-usuario.component';
import { EditorCursoComponent } from './components/editor-curso/editor-curso.component';
import { HomeComponent } from './components/home/home.component';
import { PerfilUsuarioComponent } from './components/perfil-usuario/perfil-usuario.component';
import { ReproductionComponent } from './components/reproduction/reproduction.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'cursosUsuario', component: CursosUsuarioComponent },
	{ path: 'curso/:id_curso', component: CursoComponent },
	{ path: 'repro/:id_usuario/:id_curso', component: ReproductionComponent },
	{ path: 'editorCurso/:id_curso', component: EditorCursoComponent },
	{ path: 'perfil', component: PerfilUsuarioComponent },
	{ path: '**', redirectTo: '' },
];
