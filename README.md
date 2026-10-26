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

The workflow in `.github/workflows/ci-cd.yml` runs a production build for pull requests targeting `main` or `develop`. After a push to `develop`, it deploys a preview to the Firebase Hosting channel `develop`; after a push to `main`, it deploys to the live Hosting site.

Before merging changes to either branch, configure these values in **GitHub → Settings → Secrets and variables → Actions**:

- Repository variable `FIREBASE_PROJECT_ID`: the Firebase project ID.
- Repository variable `API_URL`: the base URL of the deployed backend API, without a trailing slash. This is included in the public frontend bundle, so it must not contain credentials or other secrets.
- Repository secret `FIREBASE_SERVICE_ACCOUNT`: the JSON key for a service account with permission to deploy Firebase Hosting.

The production build uses `API_URL` instead of the local `http://localhost:8080` URL. The workflow intentionally fails before deployment if this variable is missing. The backend must also allow requests, including credentialed requests if applicable, from the Firebase Hosting domains.

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
