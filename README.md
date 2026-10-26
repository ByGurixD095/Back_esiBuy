# FrontEsiBuy

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.2.0.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## CI/CD with GitHub Actions and Firebase Hosting

The workflow in `.github/workflows/firebase-hosting.yml` builds pull requests targeting `master` or `develop` and publishes a Firebase Hosting preview for pull requests from this repository. After a push to `master`, it deploys the production site to Firebase Hosting. The Hosting configuration serves the Angular build from `dist/Front_esiBuy/browser` and routes application paths back to `index.html`.

In **GitHub → Settings → Secrets and variables → Actions**, create the repository secret `FIREBASE_SERVICE_ACCOUNT_ESIBUY` with the JSON service-account key that has permission to deploy to Firebase project `esibuy`. The project reference is stored in `.firebaserc`.

You can optionally set the repository variable `API_URL` to the public backend base URL, without a trailing slash. If it is not set, the website still deploys but API calls will not reach the production backend. This URL is included in the public frontend bundle and must not contain credentials or other secrets. Configure the backend CORS policy to allow the Firebase Hosting domain.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## User registration

The `/register` page lets visitors choose between creating a customer account and registering as a seller.
The customer form submits to `POST /users/register/clientes` with personal details, DNI, and birth date.
The seller form submits to `POST /users/register/vendedor` with commercial name, CIF/NIF, and primary category ID.
Both requests also include the optional phone and profile-image URL fields. When registration succeeds,
the frontend signs in with `POST /users/login`, sends credentials with cookies enabled, and navigates to
the products page when `mfaStatus` is `SUCCESS`. MFA setup and verification are not yet implemented;
the frontend reports when the backend requires either step.

## Category administration

Administrators can create a category at `/admin/categorias/nueva` using
`POST /admin/categorias`. Category names must be unique.

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
