{{- /*
Common Helm template helpers for Veggie Shop
All comments are in English as requested.
*/ -}}

{{/*
Return the chart's name, allowing override via .Values.nameOverride.
*/}}
{{- define "veggie-shop.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Return the fully qualified name of the release.
If .Values.fullnameOverride is set, it is used directly.
Otherwise: "<release name>-<chart name>", trimmed to 63 chars.
*/}}
{{- define "veggie-shop.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := include "veggie-shop.name" . -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Chart label used by some tooling (replaces '+' in versions with '_').
*/}}
{{- define "veggie-shop.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" -}}
{{- end -}}

{{/*
Common labels applied to most resources.
*/}}
{{- define "veggie-shop.labels" -}}
helm.sh/chart: {{ include "veggie-shop.chart" . }}
app.kubernetes.io/name: {{ include "veggie-shop.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- with .Values.labels }}
{{ toYaml . }}
{{- end }}
{{- end -}}

{{/*
Selector labels must be stable across rollouts (used by Deployments/Services).
*/}}
{{- define "veggie-shop.selectorLabels" -}}
app.kubernetes.io/name: {{ include "veggie-shop.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*
Return the name of the service account to use.
If serviceAccount.create=true, default to <fullname>; otherwise default to "default".
*/}}
{{- define "veggie-shop.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
{{- default (include "veggie-shop.fullname" .) .Values.serviceAccount.name -}}
{{- else -}}
{{- default "default" .Values.serviceAccount.name -}}
{{- end -}}
{{- end -}}
