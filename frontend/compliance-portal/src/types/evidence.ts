import { z } from 'zod';

export const EvidenceStatusSchema = z.enum(['DRAFT', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'LOCKED']);

export const EvidenceSchema = z.object({
  id: z.uuid(),
  tenantId: z.string(),
  title: z.string(),
  description: z.string(),
  type: z.string(),
  sourceSystem: z.string(),
  owner: z.string(),
  approver: z.string().nullable().optional(),
  status: EvidenceStatusSchema,
  version: z.number(),
  createdAt: z.string(),
  updatedAt: z.string(),
  approvedAt: z.string().nullable().optional(),
  references: z.record(z.string(), z.string()).default({}),
  attachmentIds: z.array(z.string()).default([]),
});

export const CreateEvidenceRequestSchema = z.object({
  title: z.string().min(1).max(500),
  description: z.string().min(1),
  type: z.string().min(1),
  sourceSystem: z.string().min(1),
  owner: z.string().min(1),
  references: z.record(z.string(), z.string()).optional().default({}),
});

export const UpdateEvidenceStatusRequestSchema = z.object({
  status: EvidenceStatusSchema,
});

export const ApiResponseSchema = <T extends z.ZodTypeAny>(dataSchema: T) =>
  z.object({
    success: z.boolean(),
    data: dataSchema.nullable(),
    error: z.object({
      code: z.string(),
      message: z.string(),
      details: z.record(z.string(), z.unknown()).optional().nullable(),
    }).nullable().optional(),
    timestamp: z.string(),
  });

export type Evidence = z.infer<typeof EvidenceSchema>;
export type CreateEvidenceRequest = z.infer<typeof CreateEvidenceRequestSchema>;
export type UpdateEvidenceStatusRequest = z.infer<typeof UpdateEvidenceStatusRequestSchema>;
