import { Routes } from '@angular/router';
import { authGuard, publicGuard, roleGuard } from './core/guards/auth.guard';

const escritura = roleGuard(['ADMIN', 'USER']); // crear / editar
const soloAdmin = roleGuard(['ADMIN']);

/**
 * Enrutamiento de AgroControl (carga diferida de cada vista).
 *  - /login y /registro: públicas (si ya hay sesión, redirigen al inicio del rol)
 *  - resto de rutas: requieren sesión (authGuard) y se muestran dentro del ShellComponent
 *  - formularios de creación/edición: solo ADMIN y USER; /usuarios: solo ADMIN
 */
export const routes: Routes = [
  {
    path: 'login',
    canActivate: [publicGuard],
    title: 'Iniciar sesión | AgroControl',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'registro',
    canActivate: [publicGuard],
    title: 'Crear cuenta | AgroControl',
    loadComponent: () => import('./features/auth/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./shared/layout/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        title: 'Panel general | AgroControl',
        loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },

      // ----- Productores -----
      {
        path: 'productores',
        children: [
          {
            path: '',
            title: 'Productores | AgroControl',
            loadComponent: () => import('./features/productores/productor-lista.component').then((m) => m.ProductorListaComponent),
          },
          {
            path: 'nuevo',
            canActivate: [escritura],
            title: 'Nuevo productor | AgroControl',
            loadComponent: () => import('./features/productores/productor-form.component').then((m) => m.ProductorFormComponent),
          },
          {
            path: ':id/editar',
            canActivate: [escritura],
            title: 'Editar productor | AgroControl',
            loadComponent: () => import('./features/productores/productor-form.component').then((m) => m.ProductorFormComponent),
          },
          {
            path: ':id',
            title: 'Detalle de productor | AgroControl',
            loadComponent: () => import('./features/productores/productor-detalle.component').then((m) => m.ProductorDetalleComponent),
          },
        ],
      },

      // ----- Parcelas -----
      {
        path: 'parcelas',
        children: [
          {
            path: '',
            title: 'Parcelas | AgroControl',
            loadComponent: () => import('./features/parcelas/parcela-lista.component').then((m) => m.ParcelaListaComponent),
          },
          {
            path: 'nueva',
            canActivate: [escritura],
            title: 'Nueva parcela | AgroControl',
            loadComponent: () => import('./features/parcelas/parcela-form.component').then((m) => m.ParcelaFormComponent),
          },
          {
            path: ':id/editar',
            canActivate: [escritura],
            title: 'Editar parcela | AgroControl',
            loadComponent: () => import('./features/parcelas/parcela-form.component').then((m) => m.ParcelaFormComponent),
          },
          {
            path: ':id',
            title: 'Detalle de parcela | AgroControl',
            loadComponent: () => import('./features/parcelas/parcela-detalle.component').then((m) => m.ParcelaDetalleComponent),
          },
        ],
      },

      // ----- Cultivos -----
      {
        path: 'cultivos',
        children: [
          {
            path: '',
            title: 'Cultivos | AgroControl',
            loadComponent: () => import('./features/cultivos/cultivo-lista.component').then((m) => m.CultivoListaComponent),
          },
          {
            path: 'nuevo',
            canActivate: [escritura],
            title: 'Nuevo cultivo | AgroControl',
            loadComponent: () => import('./features/cultivos/cultivo-form.component').then((m) => m.CultivoFormComponent),
          },
          {
            path: ':id/editar',
            canActivate: [escritura],
            title: 'Editar cultivo | AgroControl',
            loadComponent: () => import('./features/cultivos/cultivo-form.component').then((m) => m.CultivoFormComponent),
          },
          {
            path: ':id',
            title: 'Detalle de cultivo | AgroControl',
            loadComponent: () => import('./features/cultivos/cultivo-detalle.component').then((m) => m.CultivoDetalleComponent),
          },
        ],
      },

      // ----- Campañas agrícolas -----
      {
        path: 'campanas',
        children: [
          {
            path: '',
            title: 'Campañas | AgroControl',
            loadComponent: () => import('./features/campanas/campana-lista.component').then((m) => m.CampanaListaComponent),
          },
          {
            path: 'nueva',
            canActivate: [escritura],
            title: 'Nueva campaña | AgroControl',
            loadComponent: () => import('./features/campanas/campana-form.component').then((m) => m.CampanaFormComponent),
          },
          {
            path: ':id/editar',
            canActivate: [escritura],
            title: 'Editar campaña | AgroControl',
            loadComponent: () => import('./features/campanas/campana-form.component').then((m) => m.CampanaFormComponent),
          },
          {
            path: ':id',
            title: 'Detalle de campaña | AgroControl',
            loadComponent: () => import('./features/campanas/campana-detalle.component').then((m) => m.CampanaDetalleComponent),
          },
        ],
      },

      // ----- Cosechas -----
      {
        path: 'cosechas',
        children: [
          {
            path: '',
            title: 'Cosechas | AgroControl',
            loadComponent: () => import('./features/cosechas/cosecha-lista.component').then((m) => m.CosechaListaComponent),
          },
          {
            path: 'nueva',
            canActivate: [escritura],
            title: 'Nueva cosecha | AgroControl',
            loadComponent: () => import('./features/cosechas/cosecha-form.component').then((m) => m.CosechaFormComponent),
          },
          {
            path: ':id/editar',
            canActivate: [escritura],
            title: 'Editar cosecha | AgroControl',
            loadComponent: () => import('./features/cosechas/cosecha-form.component').then((m) => m.CosechaFormComponent),
          },
          {
            path: ':id',
            title: 'Detalle de cosecha | AgroControl',
            loadComponent: () => import('./features/cosechas/cosecha-detalle.component').then((m) => m.CosechaDetalleComponent),
          },
        ],
      },

      // ----- Usuarios (solo ADMIN) -----
      {
        path: 'usuarios',
        canActivate: [soloAdmin],
        title: 'Usuarios | AgroControl',
        loadComponent: () => import('./features/usuarios/usuario-lista.component').then((m) => m.UsuarioListaComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
