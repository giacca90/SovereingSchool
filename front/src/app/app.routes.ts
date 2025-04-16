import { Routes } from '@angular/router';
import { ChatComponent } from './components/chat/chat/chat.component';
import { HomeChatComponent } from './components/chat/home-chat/home-chat.component';
import { CursoComponent } from './components/curso/curso.component';
import { CursosUsuarioComponent } from './components/cursos-usuario/cursos-usuario.component';
import { EditorCursoComponent } from './components/editor-curso/editor-curso.component';
import { HomeComponent } from './components/home/home.component';
import { PerfilUsuarioComponent } from './components/perfil-usuario/perfil-usuario.component';
import { PrivacyComponent } from './components/privacy/privacy.component';
import { ReproductionComponent } from './components/reproduction/reproduction.component';
import { GuestGuard } from './guard/guest.guard';
import { ProfGuard } from './guard/prof.guard';
import { UserGuard } from './guard/user.guard';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'cursosUsuario', component: CursosUsuarioComponent, canActivate: [GuestGuard] },
	{ path: 'curso/:id_curso', component: CursoComponent },
	{ path: 'tus-chat', component: HomeChatComponent, canActivate: [GuestGuard] },
	{ path: 'chat/:id_curso', component: ChatComponent, canActivate: [GuestGuard, UserGuard] },
	{ path: 'chat/:id_curso/:id_mensaje', component: ChatComponent, canActivate: [GuestGuard, UserGuard] },
	{ path: 'repro/:id_usuario/:id_curso/:id_clase', component: ReproductionComponent, canActivate: [GuestGuard, UserGuard] },
	{ path: 'editorCurso/:id_curso', component: EditorCursoComponent, canActivate: [GuestGuard, ProfGuard] },
	{ path: 'perfil', component: PerfilUsuarioComponent, canActivate: [GuestGuard] },
	{ path: 'privacy', component: PrivacyComponent },
	{ path: '**', redirectTo: '' },
];
