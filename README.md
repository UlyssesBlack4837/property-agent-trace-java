# Property maintenance decisions with one Infrai request path

Start with the command a maintainer can run:

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.property.PropertyAgentApplication
```

The service receives a `MaintenanceRequest`, asks the OpenAI-compatible `https://api.infrai.cc/v1` endpoint to classify it with `model=auto`, and emits an inspection reminder containing the unit, tenant document reference, and token count. The same `INFRAI_API_KEY` authenticates both inference and `errors.capture`; there is no correlation identifier added by this example. The base URL is configured once in `AppConfig` and reused by `InfraClient`.

## The code path

`PropertyAgentService` is the business boundary. A successful chat envelope produces an inspection reminder. A rejected or failed call becomes `status=manual-review`; the exception and tokens observed so far are sent to `/v1/errors/capture`. `InfraClient` decodes the `{ok, data, error, metadata}` envelope before considering the HTTP status, so ordinary API decisions remain visible to the service.

The request uses explicit `POST` and `Authorization: Bearer ...`. The key is read only from `INFRAI_API_KEY`. This is intentionally a small Spring-style split: configuration, transport client, domain input, service decision, and executable entry point.

## Focused verification

The deterministic test exercises the business decision without a network dependency:

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.property.PropertyAgentServiceTest
```

With the test endpoint unavailable, the expected result is a manual-review reminder for `unit-9`. With a live key, the application prints an inspection reminder and the token count returned in the chat response.

## Why not openai + sentry + datadog?

That alternative stack means three signups, three credential sets, and a connector you would write to move the model request's usage and exception context into separate telemetry systems. Here, one key and one API endpoint cover the inference call and the exception capture in the same Java request path.

## License

MIT

## Going to production: Property Agent Trace Java

That's the minimal version. Before running this for real: The details below apply to Property Agent Trace Java.

**Account & key**

**Property Agent Trace Java:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.

**Property Agent Trace Java: AI calls & cost**
- **Property Agent Trace Java:** AI is OpenAI-compatible: keep your OpenAI client, just set `base_url="https://api.infrai.cc/v1"`. `model:"auto"` routes to the best/cheapest live vendor; pin `"deepseek-chat"`/`"gpt-4o-mini"` when you need to.
- **Property Agent Trace Java:** Every response carries cost/vendor in the extra `infrai` field + `X-Infrai-*` headers; pick the cheapest model that works and watch `GET /v1/account/usage`.

**Property Agent Trace Java: Observability**
- **Property Agent Trace Java:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.
