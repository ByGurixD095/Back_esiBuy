import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AuthService } from '../../services/auth.service';
import { Pedido, PedidosService } from '../../services/pedidos.service';
import { MisPedidos } from './mis-pedidos';

describe('MisPedidos', () => {
  let fixture: ComponentFixture<MisPedidos>;
  const pedidosServiceFalso = { listar: vi.fn() };
  const authFalso = { errorMessage: (_error: unknown, fallback: string) => fallback };

  async function crear(pedidos: Pedido[]): Promise<void> {
    pedidosServiceFalso.listar.mockReturnValue(of(pedidos));
    await TestBed.configureTestingModule({
      imports: [MisPedidos],
      providers: [
        provideRouter([]),
        { provide: PedidosService, useValue: pedidosServiceFalso },
        { provide: AuthService, useValue: authFalso },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(MisPedidos);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  const html = () => fixture.nativeElement as HTMLElement;

  beforeEach(() => {
    pedidosServiceFalso.listar.mockReset();
  });

  it('muestra el estado vacío y un acceso al catálogo cuando no hay pedidos', async () => {
    await crear([]);

    expect(html().textContent).toContain('Aún no has realizado ningún pedido');
    expect(html().querySelector('a[href="/products"]')).not.toBeNull();
  });

  it('muestra los productos, el estado, el pago, el envío y el total del pedido', async () => {
    await crear([
      {
        id: 'pedido-42',
        lineas: [
          {
            productoId: 'producto-1',
            nombre: 'Mochila',
            referencia: 'MCH-1',
            urlImagen: null,
            precioUnitarioCent: 2599,
            cantidad: 2,
            subtotalCent: 5198,
          },
        ],
        direccionEnvio: {
          nombreCompleto: 'Ana García',
          direccion: 'Calle Mayor 1',
          ciudad: 'Madrid',
          provincia: 'Madrid',
          codigoPostal: '28001',
          pais: 'España',
          telefono: '+34600000000',
        },
        metodoPagoId: 'pago-1',
        tipoMetodoPago: 'TARJETA',
        aliasMetodoPago: 'Visa personal',
        referenciaMetodoPago: '**** 1234',
        totalCent: 5198,
        estado: 'RECIBIDO',
        fechaPedido: '2026-10-25T13:30:00',
      },
    ]);

    expect(html().textContent).toContain('pedido-42');
    expect(html().textContent).toContain('Mochila');
    expect(html().textContent).toContain('Recibido');
    expect(html().textContent).toContain('Visa personal');
    expect(html().textContent).toContain('Ana García');
    expect(html().textContent).toContain('51,98');
  });
});
