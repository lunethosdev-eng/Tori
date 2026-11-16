# Tori Community Plugins — SDK y guía para desarrolladores

**Formato comunitario:** `plugin.json` + recursos locales en ZIP.  
**Versión del formato:** `1.0`.  
**API nativa para módulos confiables:** `ToriPluginApi.VERSION = 1`.

## 1. Dos vías de extensión

### Plugins de la comunidad (sin código ejecutable)

Los paquetes comunitarios son declarativos. Tori lee `plugin.json` y recursos de `assets/`; no carga clases, DEX, JAR, APK, bibliotecas nativas, JavaScript ni scripts. Las funciones disponibles dependen de las capacidades que Tori implemente y permita explícitamente.

Capacidades comunitarias actualmente admitidas:

- `ui.theme`: fondo, color de fondo, sonido de clic y preset de movimiento.
- `ui.home_widgets`: tarjetas del Home con destinos internos permitidos.
- `ui.icons`: recursos de iconos para superficies que Tori habilite.

Una capacidad no incluida en esta lista no se puede activar añadiéndola al manifiesto. Las capacidades futuras requieren una versión de Tori que las implemente.

### Extensiones Kotlin nativas (módulos de confianza)

Los módulos Kotlin que se compilan dentro de una APK pueden implementar `ToriNativeExtension` y registrarse explícitamente mediante `ToriPluginRegistry.register(...)`. Esta API no carga código de archivos ZIP: el módulo debe formar parte del código revisado y compilado de Tori.

Ejemplo:

```kotlin
class MyHomeExtension : ToriNativeExtension {
    override val descriptor = ToriPluginDescriptor(
        id = "com.example.my-home",
        name = "My Home Extension",
        version = "1.0.0",
        capabilities = setOf("ui.home_widgets")
    )

    override fun contributions() = listOf(
        ToriPluginContribution.HomeCard(
            id = "collection-shortcut",
            title = "My collection",
            subtitle = "Open my library",
            destination = "library"
        )
    )
}

// Registrar desde el bootstrap de un módulo confiable:
ToriPluginRegistry.register(MyHomeExtension())
```

La API valida ID, versión de API, capacidades, longitud de textos y destinos. Los destinos válidos son `home`, `library`, `search`, `profile`, `settings` y `player`. La extensión puede retirarse con `ToriPluginRegistry.unregister(id)`.

## 2. Estructura de un paquete comunitario

```text
my-plugin.zip
├── plugin.json
└── assets/
    ├── images/background.webp
    ├── images/card-icon.png
    ├── audio/click.ogg
    └── animations/motion.json
```

`plugin.json` debe estar en la raíz del ZIP, no dentro de una carpeta contenedora.

## 3. Manifiesto de ejemplo

```json
{
  "id": "community.neon-night",
  "name": "Neon Night",
  "version": "1.0.0",
  "type": "theme",
  "minAppVersion": "1.0.0",
  "permissions": ["ui.theme", "ui.home_widgets", "ui.icons"],
  "ui": {
    "theme": {
      "background": "assets/images/background.webp",
      "backgroundColor": "#0C0718",
      "clickSound": "assets/audio/click.ogg",
      "motionAsset": "assets/animations/motion.json"
    },
    "homeCards": [
      {
        "title": "Neon Collection",
        "subtitle": "Open your library",
        "action": "open:library",
        "iconAsset": "assets/images/card-icon.png"
      }
    ]
  }
}
```

### Campos

| Campo | Regla |
|---|---|
| `id` | 2–80 caracteres; letras, números, punto, guion o guion bajo |
| `name` | Obligatorio, máximo 80 caracteres |
| `version` | SemVer `major.minor.patch` |
| `type` | `ui`, `function`, `catalog` o `theme` |
| `minAppVersion` | Versión mínima de Tori |
| `permissions` | Solo capacidades admitidas; las desconocidas hacen fallar la instalación |
| `ui.theme.background` | Ruta local dentro de `assets/` |
| `ui.theme.backgroundColor` | `#RRGGBB` o `#AARRGGBB` |
| `ui.theme.clickSound` | Audio local para controles compatibles |
| `ui.theme.motionAsset` | JSON con preset de movimiento |
| `ui.homeCards` | Tarjetas declarativas del Home |
| `ui.homeCards[].action` | `open:home`, `open:library`, `open:search`, `open:profile`, `open:settings` o `open:player` |
| `ui.homeCards[].iconAsset` | Recurso local; requiere `ui.icons` |

Campos no soportados no amplían las capacidades del paquete. No se aceptan URL remotas como assets.

## 4. Preset de movimiento

`assets/animations/motion.json`:

```json
{
  "pressScale": 0.965,
  "dampingRatio": 0.58,
  "stiffness": 420
}
```

Rangos admitidos por el motor: `pressScale` 0.90–0.99, `dampingRatio` 0.15–1.0 y `stiffness` 80–1200. Tori adapta el preset a sus preferencias de movimiento y rendimiento. No se admiten timelines ni scripts arbitrarios.

## 5. Recursos y límites

Extensiones permitidas: `png`, `jpg`, `jpeg`, `webp`, `gif`, `wav`, `ogg`, `mp3`, `json` y `ttf`.

- ZIP comprimido: máximo 25 MB.
- Contenido extraído: máximo 50 MB.
- Entradas ZIP: máximo 256.
- `plugin.json`: máximo 128 KB.
- Todos los archivos, excepto `plugin.json`, deben estar dentro de `assets/`.
- No usar rutas absolutas, `..`, `.`, segmentos vacíos, rutas duplicadas ni rutas fuera del paquete.

Las fuentes TTF se conservan como recursos, pero la aplicación tipográfica de fuentes comunitarias no está habilitada todavía.

## 6. Publicación en Supabase

1. Crear el registro en `public.plugins` con `status = 'draft'`.
2. Subir el ZIP al bucket `tori-plugin-packages`.
3. Calcular SHA-256 del ZIP final (64 caracteres hexadecimales).
4. Guardar `package_url`, `current_version`, `author_name`, `icon_url` y descripción.
5. Tras revisar paquete y manifiesto, cambiar `status` a `published`.

La app consulta únicamente registros publicados. La instalación invoca `POST /rest/v1/rpc/plugin_download` con `{"p_id":"community.neon-night"}`; el RPC incrementa descargas y devuelve la URL oficial. La app exige que la URL pertenezca al bucket oficial, comprueba SHA-256 y compara ID/versión del manifiesto antes de instalar.

## 7. Instalación y confianza

La importación manual usa el selector de documentos Android y aplica las mismas validaciones de rutas, estructura, extensiones, capacidades y límites. El ZIP manual no tiene un hash publicado por el catálogo contra el cual verificar su origen: Tori calcula su SHA-256 para identificarlo, pero Android lo marca como instalación manual y el usuario debe confiar en su distribuidor.

Enlaces oficiales de instalación deben apuntar al bucket exacto de `tori-plugin-packages` y siempre requieren confirmación del usuario.

## 8. Seguridad y permisos

- Las capacidades de plugin son permisos internos de Tori, no permisos Android.
- Un plugin no puede añadir ni concederse permisos Android dinámicamente.
- Funciones que requieran cámara, micrófono, ubicación u otros permisos del sistema necesitan implementación nativa, revisión, declaración en `AndroidManifest.xml` y consentimiento del usuario.
- Las capacidades desconocidas o duplicadas hacen fallar la instalación; no se conceden silenciosamente.
- Desactivar un plugin impide que sus contribuciones declarativas se apliquen. Desinstalar elimina sus archivos privados.
- Las actualizaciones se preparan en staging y conservan la versión instalada anterior si falla la sustitución.

## 9. Checklist de publicación

- [ ] `plugin.json` está en la raíz.
- [ ] ID, nombre y SemVer son válidos.
- [ ] Todos los recursos referenciados existen.
- [ ] Solo se declaran capacidades admitidas y sin duplicados.
- [ ] Todas las acciones usan destinos permitidos.
- [ ] No hay código, binarios ejecutables ni scripts.
- [ ] ZIP y contenido extraído respetan los límites.
- [ ] SHA-256 calculado después de generar el ZIP definitivo.
- [ ] El paquete se probó instalado, activado, desactivado y desinstalado.
- [ ] La ficha del catálogo coincide con el manifiesto y el ZIP.
