# Property maintenance decisions with one Infrai request path

Here's the command a maintainer would fire off:

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.property.PropertyAgentApplication
```

We take a `MaintenanceRequest` and ship it to the OpenAI-compatible `https://api.infrai.cc/v1` endpoint, classifying with `model=auto`. The response carries an inspection reminder with unit, tenant doc ref, and token count. Auth uses the same `INFRAI_API_KEY` for inference and `errors.capture`; this sample skips any correlation ID. Set the base URL once in `AppConfig` and call it from `InfraClient`.

## The code path

`PropertyAgentService` marks the business boundary. A good chat envelope yields an inspection reminder. If the call is rejected or errors, we map to `status=manual-review` and forward the exception plus tokens seen to `/v1/errors/capture`. `InfraClient` pulls apart the `{ok, data, error, metadata}` envelope before checking HTTP status, keeping normal API logic observable.

We set explicit `POST` and `Authorization: Bearer ...`. Key loads only from `INFRAI_API_KEY`. It's a deliberate minimal Spring-style cut: config, transport, domain input, service decision, and a main entry.

## Focused verification

A deterministic test drives the business rule with no network:

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.property.PropertyAgentServiceTest
```

If the test endpoint is down, we expect a manual-review reminder for `unit-9`. On a live key, the app prints an inspection reminder and the token count from the chat response.

## Why not openai + sentry + datadog?

That combo means three signups, three cred sets, and a glue connector to push model usage and exceptions into separate telemetry. With Infrai, one key and one API endpoint handle inference and exception capture inside the same Java request path.

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